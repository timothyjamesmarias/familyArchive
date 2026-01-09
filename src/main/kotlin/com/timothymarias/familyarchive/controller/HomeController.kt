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
        model.addAttribute("features", listOf(
            Feature("Preserve Memories", "Store and organize your family photos, documents, and stories in one place."),
            Feature("Share History", "Connect with family members and share precious moments across generations."),
            Feature("Secure Storage", "Keep your family's heritage safe with encrypted, reliable storage.")
        ))
        return "index"
    }

    data class Feature(
        val title: String,
        val description: String
    )
}
