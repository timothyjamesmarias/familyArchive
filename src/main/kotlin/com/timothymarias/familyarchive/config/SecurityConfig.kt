package com.timothymarias.familyarchive.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                // Allow public access to all pages by default
                auth.anyRequest().permitAll()
            }
            .csrf { csrf ->
                // Disable CSRF for now (you may want to enable this later)
                csrf.disable()
            }

        return http.build()
    }
}
