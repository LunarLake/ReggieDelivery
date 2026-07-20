package com.wyc.reggie.filter;

import com.wyc.reggie.common.BaseContext;
import com.wyc.reggie.common.R;
import com.wyc.reggie.common.WebUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

// 登录检查过滤器注解，用于拦截所有请求，检查用户是否已登录
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher(); // 路径匹配器，用于匹配请求路径
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 转换为 HttpServletRequest 和 HttpServletResponse，以便获取请求 URI
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();
        String [] urls = new String[]{  // 定义放行的请求路径
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",       // 文件上传/下载（菜品图片等）
                "/user/sendMsg",
                "/user/login",
                "/user/loginout"
        };
        boolean check = check(urls, requestURI); // 检查请求路径是否在放行列表中

        if (check) {
            chain.doFilter(request, response); // 如果在放行列表中，直接放行
            return;
        }
        Long employeeId = (Long) httpRequest.getSession().getAttribute("employee");
        if (employeeId != null) {
            BaseContext.setCurrentId(employeeId);
            try {
                chain.doFilter(request, response); // 如果已登录，放行请求
            } finally {
                BaseContext.remove();
            }
            return;
        }
        WebUtils.renderJson(httpResponse, R.error("NOTLOGIN")); // 与前端拦截器一致，触发跳转登录页
    }
    public boolean check(String[] urls, String requestURI) { // 检查请求路径是否在放行列表中
        for (String url : urls) {
            if (PATH_MATCHER.match(url, requestURI)) {
                return true;
            }
        }
        return false;
    }

}
