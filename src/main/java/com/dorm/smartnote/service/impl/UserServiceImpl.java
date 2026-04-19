package com.dorm.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.smartnote.entity.User;
import com.dorm.smartnote.mapper.UserMapper;
import com.dorm.smartnote.model.dto.UserLoginDTO;
import com.dorm.smartnote.model.dto.UserRegisterDTO;
import com.dorm.smartnote.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务实现类
 * 满足 1.1 技术栈约束 (SpringBoot 3.5.x)
 * 满足 3.1.1 安全约束 (不存储明文密码)
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 引入加密工具
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public boolean register(UserRegisterDTO registerDTO) {
        // 1. 账号唯一性检查：根据邮箱判断
        User existingUser = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, registerDTO.getEmail()));
        if (existingUser != null) {
            return false;
        }

        // 2. 对象转换
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);

        // 3. 核心安全操作：加密密码后再存入数据库
        String securePassword = passwordEncoder.encode(registerDTO.getPassword());
        user.setPassword(securePassword);

        // 4. 保存到数据库
        return this.save(user);
    }

    @Override
    public User login(UserLoginDTO loginDTO) {
        // 1. 文档要求：支持邮箱或手机号登录
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, loginDTO.getAccount())
                .or()
                .eq(User::getPhone, loginDTO.getAccount()));

        // 2. 校验加密后的密码是否匹配
        if (user != null && passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return user;
        }

        return null; // 登录失败
    }
}