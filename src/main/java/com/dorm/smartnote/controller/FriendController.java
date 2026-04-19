package com.dorm.smartnote.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.smartnote.common.Result;
import com.dorm.smartnote.entity.Friend;
import com.dorm.smartnote.entity.User;
import com.dorm.smartnote.mapper.FriendMapper;
import com.dorm.smartnote.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friend")
@CrossOrigin
public class FriendController {

    private final FriendMapper friendMapper;
    private final UserMapper userMapper;

    public FriendController(FriendMapper friendMapper, UserMapper userMapper) {
        this.friendMapper = friendMapper;
        this.userMapper = userMapper;
    }

    private Long getUserId(String token) {
        return UserController.tokenStore.get(token);
    }

    // 1. 发送好友申请 (根据邮箱搜索)
    // 1. 发送好友申请 (根据邮箱搜索)
    @PostMapping("/add")
    public Result<String> addFriend(@RequestParam String email, @RequestHeader("Authorization") String token) {
        Long myId = getUserId(token);

        // 【修正点】使用 LambdaQueryWrapper 配合传入的 email 参数进行查询
        // 这样就使用了 'email' 变量，且定义了 'target' 变量
        User target = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));

        // 检查 target 是否为空
        if (target == null) {
            return Result.error("未找到该用户");
        }

        // 检查是否是添加自己
        if (target.getId().equals(myId)) {
            return Result.error("不能添加自己");
        }

        // 检查是否已经是好友或已经发送过申请（防止重复插入）
        Friend exists = friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, myId)
                .eq(Friend::getFriendId, target.getId()));
        if (exists != null) {
            return Result.error("请勿重复发送申请或已经是好友");
        }

        Friend friend = new Friend();
        friend.setUserId(myId);
        friend.setFriendId(target.getId()); // 这里现在可以解析 'target' 了
        friend.setStatus(0); // 0: 待通过

        friendMapper.insert(friend);
        return Result.success("申请已发送");
    }
    // 2. 查看好友列表 (已通过的)
    @GetMapping("/list")
    public Result<List<User>> listFriends(@RequestHeader("Authorization") String token) {
        Long myId = getUserId(token);
        List<Friend> relations = friendMapper.selectList(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, myId).eq(Friend::getStatus, 1));

        List<Long> friendIds = relations.stream().map(Friend::getFriendId).collect(Collectors.toList());
        if (friendIds.isEmpty()) return Result.success(List.of());

        return Result.success(userMapper.selectBatchIds(friendIds));
    }

    // 3. 查看待处理的申请
    @GetMapping("/pending")
    public Result<List<Friend>> getPending(@RequestHeader("Authorization") String token) {
        Long myId = getUserId(token);
        return Result.success(friendMapper.selectList(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getFriendId, myId).eq(Friend::getStatus, 0)));
    }

    // 4. 同意申请
    @PostMapping("/accept/{id}")
    public Result<String> accept(@PathVariable Long id) {
        Friend f = friendMapper.selectById(id);
        f.setStatus(1);
        friendMapper.updateById(f);

        // 互为好友逻辑：反向也插入一条
        Friend reverse = new Friend();
        reverse.setUserId(f.getFriendId());
        reverse.setFriendId(f.getUserId());
        reverse.setStatus(1);
        friendMapper.insert(reverse);

        return Result.success("已添加好友");
    }
}