package com.dorm.smartnote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friend")
public class Friend {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;      // 对应数据库 user_id

    private Long friendId;    // 对应数据库 friend_id

    /**
     * 状态：0-待通过, 1-已通过, 2-拉黑
     */
    private Integer status;

    private LocalDateTime createTime;
}