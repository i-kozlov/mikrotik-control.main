package com.baybaka.mikrotikcontrol.presentation.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * Контроллер для авторизации
 */
@Controller
class AuthController {
    
    /**
     * Страница входа
     */
    @GetMapping("/login")
    fun login(): String {
        return "login"
    }
}
