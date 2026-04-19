package com.dorm.smartnote.common.context;

/**
 * 用户上下文工具类
 * 用于在当前线程中存储和获取登录用户 ID
 * 符合任务书“用户隔离”与“架构低耦合”要求
 */
public class UserContext {

    // 使用 ThreadLocal 存储用户 ID，确保线程安全
    private static final ThreadLocal<Long> userHolder = new ThreadLocal<>();

    /**
     * 将解析出来的用户 ID 存入上下文
     * 通常在拦截器 (Interceptor) 中调用
     */
    public static void setUserId(Long userId) {
        userHolder.set(userId);
    }

    /**
     * 获取当前线程中的用户 ID
     * 在 Service 或 Mapper 中调用，用于 SQL 过滤
     */
    public static Long getUserId() {
        return userHolder.get();
    }

    /**
     * 清理用户信息，防止内存泄漏
     * 必须在请求结束（拦截器的 afterCompletion）中调用
     */
    public static void remove() {
        userHolder.remove();
    }
}