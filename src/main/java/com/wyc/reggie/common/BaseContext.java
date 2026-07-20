package com.wyc.reggie.common;

// 基于ThreadLocal封装工具类，用于保存和获取当前登录用户的id
public class BaseContext {
    // ThreadLocal是一个线程局部变量，每个线程都有自己的独立变量副本，互不干扰
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    // 设置当前登录用户的id
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }
    // 获取当前登录用户的id
    public static Long getCurrentId() {
        return threadLocal.get();
    }
    // 清除当前线程的ThreadLocal副本，防止内存泄漏
    public static void remove() {
        threadLocal.remove();
    }
}
