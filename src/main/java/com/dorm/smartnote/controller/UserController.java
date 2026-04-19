package com.dorm.smartnote.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.smartnote.common.Result;
import com.dorm.smartnote.entity.User;
import com.dorm.smartnote.mapper.UserMapper;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    private final UserMapper userMapper;
    public static ConcurrentHashMap<String, Long> tokenStore = new ConcurrentHashMap<>();

    public UserController(UserMapper userMapper) { this.userMapper = userMapper; }

    // 密码加密工具
    private String encrypt(String password) {
        // 加盐MD5，避免简单密码被破解
        return DigestUtils.md5DigestAsHex((password + "smart_note_salt").getBytes());
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        // 1. 正则校验邮箱和手机号
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        String phoneRegex = "^1[3-9]\\d{9}$";
        if (!user.getEmail().matches(emailRegex)) return Result.error("邮箱格式不正确");
        if (user.getPhone() != null && !user.getPhone().matches(phoneRegex)) return Result.error("手机号格式不正确");

        // 2. 密码长度基础校验 (满足文档 3.1.1 约束)
        if (user.getPassword() == null || user.getPassword().length() < 6 || user.getPassword().length() > 20) {
            return Result.error("密码长度必须在 6 到 20 位之间");
        }

        // 3. 查重 (避免你之前遇到的 Duplicate entry 报错)
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())) > 0) {
            return Result.error("用户名已被占用，请更换");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail())) > 0) {
            return Result.error("邮箱已被注册");
        }

        // 4. 密码加密存储
        user.setPassword(encrypt(user.getPassword()));
        userMapper.insert(user);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody User user) {
        String encryptedPwd = encrypt(user.getPassword());
        // 支持邮箱或手机号登录
        User dbUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPassword, encryptedPwd)
                .and(w -> w.eq(User::getEmail, user.getEmail()).or().eq(User::getPhone, user.getEmail())));

        if (dbUser == null) return Result.error("账号或密码错误");

        String token = UUID.randomUUID().toString();
        tokenStore.put(token, dbUser.getId());
        return Result.success(token);
    }

    // 获取个人信息
    @GetMapping("/info")
    public Result<User> getInfo(@RequestHeader("Authorization") String token) {
        Long userId = tokenStore.get(token);
        if (userId == null) return Result.error("未登录");
        User user = userMapper.selectById(userId);
        user.setPassword(null); // 脱敏，不返回密码
        return Result.success(user);
    }

    // 修改个人信息 (昵称、座右铭等)
    @PostMapping("/updateInfo")
    public Result<String> updateInfo(@RequestBody User user, @RequestHeader("Authorization") String token) {
        Long userId = tokenStore.get(token);
        if (userId == null) return Result.error("未登录");
        user.setId(userId);
        userMapper.updateById(user);
        return Result.success("更新成功");
    }
}