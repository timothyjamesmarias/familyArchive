package com.timothymarias.familyarchive.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {

    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("title", "Welcome to Family Archive")
        model.addAttribute("message", "Your family's stories, preserved for generations.")
        return "index"
    }
}
