package com.timothymarias.familyarchive.controller.admin

import com.timothymarias.familyarchive.repository.UserRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Example admin controller.
 * Notice how it doesn't need to extend a base class - the @ControllerAdvice
 * automatically adds common attributes to all controllers in the admin package.
 */
@Controller
@RequestMapping("/admin")
class AdminDashboardController(
    private val userRepository: UserRepository
) {

    @GetMapping("/dashboard")
    fun dashboard(model: Model): String {
        model.addAttribute("pageTitle", "Dashboard")
        model.addAttribute("userCount", userRepository.count())

        // The currentUser is automatically added by AdminControllerAdvice
        return "admin/dashboard"
    }
}
