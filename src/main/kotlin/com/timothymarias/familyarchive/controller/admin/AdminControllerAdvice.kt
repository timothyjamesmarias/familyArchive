package com.timothymarias.familyarchive.controller.admin

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

/**
 * Controller advice that applies to all admin controllers.
 * it adds common model attributes to all admin pages automatically.
 */
@ControllerAdvice(basePackages = ["com.timothymarias.familyarchive.controller.admin"])
class AdminControllerAdvice {

    /**
     * Adds the current authenticated user to the model for all admin pages.
     * This allows templates to access user info without each controller adding it.
     */
    @ModelAttribute
    fun addUserToModel(
        model: Model,
        @AuthenticationPrincipal currentUser: UserDetails?
    ) {
        currentUser?.let {
            model.addAttribute("currentUser", it)
        }
    }

    /**
     * You can add more common attributes here, such as:
     * - Site settings
     * - Common navigation items
     * - Feature flags
     * - etc.
     */
}
