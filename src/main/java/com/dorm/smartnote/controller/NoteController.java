package com.dorm.smartnote.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.smartnote.common.Constants;
import com.dorm.smartnote.common.Result;
import com.dorm.smartnote.entity.Friend;
import com.dorm.smartnote.entity.Note;
import com.dorm.smartnote.entity.User;
import com.dorm.smartnote.mapper.FriendMapper;
import com.dorm.smartnote.mapper.NoteMapper;
import com.dorm.smartnote.mapper.UserMapper;
import com.dorm.smartnote.service.NoteService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/note")
@CrossOrigin
public class NoteController {

    private final NoteService noteService;
    private final NoteMapper noteMapper;
    private final FriendMapper friendMapper;
    private final UserMapper userMapper;
    private final ChatClient chatClient;

    public NoteController(NoteService noteService,
                          NoteMapper noteMapper,
                          FriendMapper friendMapper,
                          UserMapper userMapper) {
        this.noteService = noteService;
        this.noteMapper = noteMapper;
        this.friendMapper = friendMapper;
        this.userMapper = userMapper;

        String apiKey = "sk-OGhbpSKtnFNCFmc1hLCD2c4BD6YLEZ1DYpfBgPXz22thujJ4";
        String baseUrl = "https://api.chatanywhere.tech";
        this.chatClient = ChatClient.builder(new OpenAiChatModel(new OpenAiApi(baseUrl, apiKey))).build();
    }

    private Long getUserId(String token) {
        if (token == null) return null;
        return UserController.tokenStore.get(token);
    }

    // 1. 获取列表 (包含 Tags 展示)
    @GetMapping("/list")
    public Result<List<Note>> list(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestParam(required = false) String keyword) {
        Long userId = getUserId(token);
        if (userId == null) return Result.error("未登录");

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId).eq(Note::getDeleted, 0);

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 搜索时同时匹配 标题 和 标签
            wrapper.and(w -> w.like(Note::getTitle, keyword).or().like(Note::getTags, keyword));
        }

        wrapper.orderByDesc(Note::getUpdateTime);
        return Result.success(noteService.list(wrapper));
    }

    @GetMapping("/history")
    public Result<List<Note>> getHistory(@RequestHeader("Authorization") String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.error("未登录");

        // 使用 LambdaQueryWrapper 查找最近看过且未删除的笔记
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, userId)
                .eq(Note::getDeleted, 0) // 历史记录通常只看没删的
                .isNotNull(Note::getLastViewedTime) // 必须是看过的
                .orderByDesc(Note::getLastViewedTime) // 按时间倒序
                .last("LIMIT 20"); // 只取最近20条

        List<Note> historyList = noteService.list(queryWrapper);
        return Result.success(historyList);
    }

    // 2. 详情查询 (保留 Tags 和 Motto 相关的读取)
    @GetMapping("/get/{id}")
    public Result<Note> getNote(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long currentUserId = getUserId(token);
        if (currentUserId == null) return Result.error("未登录");

        // 关键点 1：必须使用能查出 deleted=1 的方法
        // 如果 service.getById 查不出来，请参考我上条建议在 Mapper 加上 selectByIdIncludingDeleted
        Note note = noteMapper.selectByIdIncludingDeleted(id);

        if (note == null) return Result.error("笔记不存在");

        // 关键点 2：权限判断逻辑重构
        boolean isOwner = note.getUserId().equals(currentUserId);

        // 如果不是本人，且笔记已删除，禁止查看
        if (!isOwner && note.getDeleted() == 1) {
            return Result.error("笔记已不存在");
        }

        // 如果不是本人，检查公开状态和好友关系
        if (!isOwner) {
            boolean isPublic = note.getIsPublic() != null && note.getIsPublic() == Constants.PUBLIC;
            if (!isPublic) {
                LambdaQueryWrapper<Friend> fw = new LambdaQueryWrapper<>();
                fw.eq(Friend::getUserId, note.getUserId())
                        .eq(Friend::getFriendId, currentUserId)
                        .eq(Friend::getStatus, 1);
                if (friendMapper.selectCount(fw) == 0) return Result.error("无权查看");
            }
        }

        // 只有正常的笔记才更新查看时间
        if (note.getDeleted() == 0) {
            note.setLastViewedTime(LocalDateTime.now());
            noteMapper.updateById(note);
        }

        return Result.success(note);
    }
    // 3. 保存 (新增带 Tags)
    @PostMapping("/add")
    public Result<String> add(@RequestBody Note note, @RequestHeader("Authorization") String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.error("未登录");
        note.setUserId(userId);
        note.setUpdateTime(LocalDateTime.now());
        note.setDeleted(0);
        // 如果前端传了 tags 字符串，这里会直接存入数据库
        noteMapper.insert(note);
        return Result.success("保存成功");
    }

    // 4. 更新 (带 Tags 更新)
    @PostMapping("/update")
    public Result<String> update(@RequestBody Note note, @RequestHeader("Authorization") String token) {
        Long userId = getUserId(token);
        Note oldNote = noteService.getById(note.getId());
        if (oldNote == null || !oldNote.getUserId().equals(userId)) return Result.error("无权修改");

        note.setUpdateTime(LocalDateTime.now());
        noteService.updateById(note);
        return Result.success("修改成功");
    }

    // 5. 修改资料 & 密码 (支持 Motto 更新)
    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@RequestBody User userReq, @RequestHeader("Authorization") String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.error("未登录");

        User dbUser = new User();
        dbUser.setId(userId);
        // 如果前端传了密码就改密码，传了座右铭就改座右铭
        if (userReq.getPassword() != null) dbUser.setPassword(userReq.getPassword());
        if (userReq.getMotto() != null) dbUser.setMotto(userReq.getMotto());
        if (userReq.getNickname() != null) dbUser.setNickname(userReq.getNickname());
        if (userReq.getAvatar() != null) dbUser.setAvatar(userReq.getAvatar());

        userMapper.updateById(dbUser);
        return Result.success("更新成功");
    }

    // --- 以下为回收站及 AI 核心功能，已确保不丢失任何字段 ---

    @GetMapping("/trash")
    public Result<List<Note>> getTrash(@RequestHeader("Authorization") String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.error("未登录");
        return Result.success(noteMapper.selectTrashNotes(userId));
    }

    @PostMapping("/restore/{id}")
    public Result<String> restore(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = getUserId(token);
        int rows = noteMapper.restoreNoteById(id, userId);
        return rows > 0 ? Result.success("笔记已恢复") : Result.error("还原失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.error("未登录");

        // 直接使用 Mapper 的 update 配合 Wrapper，强制更新 deleted 字段
        LambdaUpdateWrapper<Note> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Note::getId, id)
                .eq(Note::getUserId, userId)
                .set(Note::getDeleted, 1); // 强制设为 1

        boolean success = noteService.update(updateWrapper);

        if (success) {
            return Result.success("已移至回收站");
        } else {
            return Result.error("删除失败：笔记可能不存在或无权操作");
        }
    }

    @PostMapping("/analyze")
    public Result<String> analyze(@RequestBody Note note, @RequestHeader("Authorization") String token) {
        if (getUserId(token) == null) return Result.error("未登录");
        try {
            String aiSummary = chatClient.prompt()
                    .user("请总结：\n" + note.getContent())
                    .call().content();
            if (note.getId() != null) {
                Note upNote = new Note();
                upNote.setId(note.getId());
                upNote.setSummary(aiSummary);
                noteMapper.updateById(upNote);
            }
            return Result.success(aiSummary);
        } catch (Exception e) { return Result.error("AI 忙"); }
    }
}