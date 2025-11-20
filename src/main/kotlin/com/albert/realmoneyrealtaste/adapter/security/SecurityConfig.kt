package com.albert.realmoneyrealtaste.adapter.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
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
                it.requestMatchers("/").permitAll()
                    .requestMatchers(HttpMethod.GET, "/posts/mine", "posts/mine/**").authenticated()
                    .requestMatchers(HttpMethod.GET, "/members/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/members/*/posts/fragment").permitAll()
                    .requestMatchers("/signup", "/members/activate", "/signin").permitAll()
                    .requestMatchers("/members/password-forgot", "/members/password-reset").permitAll()
                    .requestMatchers(HttpMethod.GET, "/posts/*/comments/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/comments/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
                    .requestMatchers("/assets/**").permitAll()
                    .requestMatchers("/error").permitAll()
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
                it.logoutUrl("/signout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
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
