package com.campusstore.config;

import com.campusstore.entity.User;
import com.campusstore.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserRepository userRepository;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/login", "/register",
                    "/css/**", "/js/**",
                    "/error"
                );
    }

    private HandlerInterceptor sessionInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object handler) throws Exception {
                HttpSession session = request.getSession(false);

                if (session != null) {
                    Long userId = (Long) session.getAttribute("userId");
                    if (userId != null) {
                        userRepository.findById(userId).ifPresent(user ->
                            request.setAttribute("currentUser", user)
                        );
                    }
                }

                String path = request.getRequestURI();
                if (path.startsWith(request.getContextPath() + "/admin")) {
                    String role = (session != null) ? (String) session.getAttribute("userRole") : null;
                    if (!"ADMIN".equals(role)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return false;
                    }
                }

                return true;
            }
        };
    }
}
