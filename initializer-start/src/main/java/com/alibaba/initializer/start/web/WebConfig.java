/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.initializer.start.web;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@ComponentScan
@Configuration
public class WebConfig implements WebMvcConfigurer {

    static private final Set<String> ALLOWED_ORIGIN_HTTP = new HashSet<>();

    static {
        ALLOWED_ORIGIN_HTTP.add("https://start.aliyun.com");
        ALLOWED_ORIGIN_HTTP.add("https://pre-start.aliyun.com");
    }

    @Bean
    public ErrorPageRegistrar errorPageRegistrar() {
        return (registry) -> {
            registry.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/404.html"));
            registry.addErrorPages(new ErrorPage("/error/index.html"));
        };
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //添加映射路径
        registry.addMapping("/**")
                //放行哪些原始域
                .allowedOrigins(ALLOWED_ORIGIN_HTTP.toArray(new String[0]))
                //是否发送Cookie信息
                .allowCredentials(true)
                //放行哪些原始域(请求方式)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                //放行哪些原始域(头部信息)
                .allowedHeaders("*");
    }

}
