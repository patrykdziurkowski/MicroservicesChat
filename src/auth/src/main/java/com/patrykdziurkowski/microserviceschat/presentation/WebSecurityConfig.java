package com.patrykdziurkowski.microserviceschat.presentation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(options -> options.disable()) // disable since no cookies
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/register", "/login", "/username", "/users/{userId}").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }
}