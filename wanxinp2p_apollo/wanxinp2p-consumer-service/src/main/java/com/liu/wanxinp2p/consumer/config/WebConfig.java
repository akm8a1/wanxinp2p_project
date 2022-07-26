package com.liu.wanxinp2p.consumer.config;

import com.liu.wanxinp2p.consumer.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 注册拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //对所有的路径进行token拦截处理
        registry.addInterceptor(new TokenInterceptor()).addPathPatterns("/**");
    }
}
