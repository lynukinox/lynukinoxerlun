package com.dorm.smartnote.common.interceptor;

import com.dorm.smartnote.common.context.UserContext;
import com.dorm.smartnote.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从 Header 获取 Token
        String token = request.getHeader("Authorization");

        if (token != null && !token.isEmpty()) {
            // 2. 解析 Token 获取用户 ID
            Long userId = JwtUtils.getUserIdFromToken(token);
            if (userId != null) {
                // 3. 核心：存入你刚才写的 UserContext
                UserContext.setUserId(userId);
                return true;
            }
        }
        // 4. 没 Token 或解析失败返回 401
        response.setStatus(401);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 5. 请求结束必须清理，防止内存泄漏
        UserContext.remove();
    }
}