package com.dorm.smartnote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.smartnote.entity.Friend;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {
    // 继承 BaseMapper 后，insert/updateById/selectList 等方法会自动生成
}