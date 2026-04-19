package com.dorm.smartnote.common.interceptor;

import com.dorm.smartnote.common.context.UserContext;
import com.dorm.smartnote.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull; // 必须导入这个
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        // 1. 获取 Token
        String token = request.getHeader("Authorization");

        if (token != null && !token.isEmpty()) {
            Long userId = JwtUtils.getUserIdFromToken(token);
            if (userId != null) {
                // 2. 存入上下文
                UserContext.setUserId(userId);
                return true;
            }
        }

        // 3. 校验失败返回 401
        response.setStatus(401);
        return false;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) throws Exception {
        // 4. 清理上下文，防止内存泄漏
        UserContext.remove();
    }
}