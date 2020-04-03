package com.singdiary.configs;

import com.singdiary.common.Description;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /*@Bean
    @Description("CORS 문제 해결을 위한 설정")
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("http://localhost:3000/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                        .allowedHeaders("Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method",
//                                            "Access-Control-Request-Headers", "Authorization")
                        .allowedMethods("*")
                        //.allowedMethods(HttpMethod.POST.name())
//                        .allowCredentials(true)
                        .maxAge(3600);
                ;
            }
        };
    }*/

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
