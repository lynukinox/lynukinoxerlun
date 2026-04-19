package com.dorm.smartnote.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User {
    @TableId
    private Long id;
    private String username;
    private String email;    // 任务书 3.1.1 要求
    private String phone;    // 任务书 3.1.1 要求
    private String password; // 数据库存储加密后的密码
    private String nickname; // 自定义昵称
    private String avatar;   // 头像
    private String motto;    // 座右铭
}