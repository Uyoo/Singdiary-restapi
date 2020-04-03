package com.singdiary.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

//authserver와 연동돼서 사용
//외부 요청이 이루어질 때 여기서 인증을 함 -> 인증이 필요하다면 /oauth/token으로 요청
// -> 토큰이 유효한지 먼저 확인
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("users");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .mvcMatchers(HttpMethod.GET,"/users/**")
                    .permitAll()
                .mvcMatchers(HttpMethod.POST, "/users/**")
                    .permitAll()
                .mvcMatchers(HttpMethod.GET, "/public/**")
                    .permitAll()
                .anyRequest()
                    .authenticated()
                .and()
            .exceptionHandling()
                .accessDeniedHandler(new OAuth2AccessDeniedHandler())
        ;
    }
}
