package com.baybaka.mikrotikcontrol.infrastructure.config

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.config.annotation.web.invoke

/**
 * Spring Security configuration for application protection
 */
@Configuration
@ConditionalOnMissingBean(SecurityFilterChain::class)
@EnableWebSecurity
class SecurityConfig(private val userProperties: UserProperties) {

    /**
     * Configures the security filter chain
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/api/**", hasRole("ADMIN"))
                authorize("/", authenticated)
                authorize("/css/**", permitAll)
                authorize("/js/**", permitAll)
                authorize("/images/**", permitAll)
                authorize("/login", permitAll)
                authorize("/actuator/**", hasRole("ADMIN"))
                authorize(anyRequest, authenticated)
            }
            formLogin {
                loginPage = "/login"
                defaultSuccessUrl("/", true)
                permitAll()
            }
            logout {
                logoutSuccessUrl = "/login?logout"
                permitAll()
            }
            rememberMe {
                tokenValiditySeconds = 86400 // 1 day
            }
            csrf {
                // Disable only for API requests, keep for web pages
                ignoringRequestMatchers(AntPathRequestMatcher("/api/**"))
            }
        }
        
        return http.build()
    }
    
    /**
     * Configures the user details service from configuration properties
     */
    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
        val userDetailsManager = InMemoryUserDetailsManager()

        // Create users from configuration
        userProperties.users.forEach { userConfig ->
            val user = User.builder()
                .username(userConfig.username)
                .password(passwordEncoder.encode(userConfig.password))
                .roles(*userConfig.roles.toTypedArray())
                .build()

            userDetailsManager.createUser(user)
        }

        return userDetailsManager
    }
    
    /**
     * Creates a password encoder
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @PostConstruct
    fun init() {
        // Log the loaded user properties
        userProperties.users.forEach { userConfig ->
            println("Loaded user: ${userConfig.username} with roles: ${userConfig.roles.joinToString(", ")}")
        }
    }
}