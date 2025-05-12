package com.baybaka.mikrotikcontrol.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * User configuration properties loaded from application.yml or application.properties
 */
@Configuration
@ConfigurationProperties(prefix = "app.security")
class UserProperties {
    /**
     * List of users with their roles
     */
    var users: List<UserConfig> = ArrayList()
    
    /**
     * User configuration
     */
    class UserConfig {
        var username: String = ""
        var password: String = ""
        var roles: List<String> = ArrayList()
    }

}