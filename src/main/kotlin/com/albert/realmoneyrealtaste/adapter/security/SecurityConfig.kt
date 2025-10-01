package com.albert.realmoneyrealtaste.adapter.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        httpSecurity
            .authorizeHttpRequests {
                it.requestMatchers("/", "/signup", "/signin").permitAll()
                    .requestMatchers("/assets/**").permitAll()
                    .requestMatchers("/api/**").permitAll()
                    .anyRequest().authenticated()
            }
            .csrf {
                it.ignoringRequestMatchers(
                    "/api/**",
                )
            }
            .formLogin {
                it.disable()
            }
            .logout {
                it.logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
            }
        return httpSecurity.build()
    }

    @Bean
    fun authenticationManager(
        authConfig: AuthenticationConfiguration,
    ): AuthenticationManager {
        return authConfig.authenticationManager
    }
}
