package com.wyc.reggie.common;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component // 交给 Spring 管理
public class WebUtils {

    private static ObjectMapper objectMapper;

    // 通过 setter 注入静态 ObjectMapper（保证工具类的静态方法能用）
    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        WebUtils.objectMapper = objectMapper;
    }

    /**
     * 将 R 对象以 JSON 格式输出到 HttpServletResponse
     */
    public static void renderJson(HttpServletResponse response, R<?> r) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        // 利用注入的 ObjectMapper 将 R 对象转为 JSON 字符串
        String json = objectMapper.writeValueAsString(r);
        response.getWriter().write(json);
    }
}