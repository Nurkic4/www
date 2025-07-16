package com.example.www.config;

// 引入Spring相关注解和安全配置类
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Security安全配置类
 */
@Configuration // 声明为配置类
public class SecurityConfig {
    /**
     * 配置安全过滤链，关闭CSRF并放行所有请求
     * @param http HttpSecurity对象
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean // 注册为Spring Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 关闭CSRF防护，所有请求无需认证即可访问
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }


} 