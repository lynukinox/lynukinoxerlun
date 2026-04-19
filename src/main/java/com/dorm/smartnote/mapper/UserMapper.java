package com.dorm.smartnote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.smartnote.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}