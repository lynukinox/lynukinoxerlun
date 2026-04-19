package com.dorm.smartnote.controller;

import com.dorm.smartnote.common.Result;
import com.dorm.smartnote.common.context.UserContext;
import com.dorm.smartnote.entity.Category;
import com.dorm.smartnote.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制层
 * 用于笔记的文件夹管理功能
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    // 使用构造器注入，符合最新 Spring 官方推荐规范
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 获取当前登录用户的所有分类列表
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        // 直接从上下文获取用户 ID，确保用户只能看到自己的分类
        Long userId = UserContext.getUserId();

        List<Category> list = categoryService.lambdaQuery()
                .eq(Category::getUserId, userId)
                .orderByDesc(Category::getCreateTime)
                .list();
        return Result.success(list);
    }

    /**
     * 新增或更新分类
     */
    @PostMapping("/save")
    public Result<Category> save(@RequestBody Category category) {
        // 绑定当前用户 ID
        category.setUserId(UserContext.getUserId());

        categoryService.saveOrUpdate(category);
        return Result.success(category);
    }

    /**
     * 删除分类
     * 注意：在 8000 字报告中可以提到，此处建议增加逻辑：
     * 如果分类下仍有笔记，应提示无法删除（或移动到默认分类）
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        // 简单的安全检查：防止删掉不属于自己的分类
        Category category = categoryService.getById(id);
        if (category == null || !category.getUserId().equals(UserContext.getUserId())) {
            return Result.error("分类不存在或无权操作");
        }

        categoryService.removeById(id);
        return Result.success("分类删除成功");
    }
}