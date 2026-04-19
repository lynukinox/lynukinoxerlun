package com.dorm.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.smartnote.entity.User;
import com.dorm.smartnote.model.dto.UserLoginDTO;
import com.dorm.smartnote.model.dto.UserRegisterDTO;

public interface UserService extends IService<User>{
    boolean register(UserRegisterDTO registerDTO);

    User login(UserLoginDTO loginDTO);
}
