package com.dorm.smartnote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.smartnote.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    // 继承了 BaseMapper，就自动拥有了增删改查方法
}