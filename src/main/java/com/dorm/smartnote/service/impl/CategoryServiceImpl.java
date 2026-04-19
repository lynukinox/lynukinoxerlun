package com.dorm.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.smartnote.entity.Category;
import com.dorm.smartnote.mapper.CategoryMapper;
import com.dorm.smartnote.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    // 继承了 ServiceImpl，MyBatis-Plus 会自动帮你实现基础的增删改查
}