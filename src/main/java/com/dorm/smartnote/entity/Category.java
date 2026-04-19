package com.dorm.smartnote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    @TableId(type = IdType.ASSIGN_ID) // 使用雪花算法生成ID
    private Long id;
    private String name;
    private Long userId;
    private LocalDateTime createTime;
}