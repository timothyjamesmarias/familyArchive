package com.timothymarias.familyarchive.seeder

import com.timothymarias.familyarchive.entity.User
import com.timothymarias.familyarchive.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/**
 * Database seeder that runs on application startup.
 * Only runs when app.seeding.enabled=true in application properties.
 * Useful for development and testing environments.
 */
@Component
@ConditionalOnProperty(
    name = ["app.seeding.enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class DatabaseSeeder(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(DatabaseSeeder::class.java)

    override fun run(args: ApplicationArguments) {
        logger.info("Starting database seeding...")
        seedUsers()
        logger.info("Database seeding completed!")
    }

    private fun seedUsers() {
        // Check if admin user already exists
        if (userRepository.findByEmail("admin@example.com") != null) {
            logger.info("Admin user already exists, skipping user seeding")
            return
        }

        logger.info("Seeding admin user...")
        val adminUser = User(
            email = "admin@example.com",
            password = passwordEncoder.encode("password") ?: throw IllegalStateException("Password encoding failed"),
            name = "Admin User"
        )
        userRepository.save(adminUser)
        logger.info("Created admin user: ${adminUser.email}")
    }

    /**
     * Add more seeding methods here as needed, e.g.:
     * - seedCategories()
     * - seedSampleContent()
     * - seedSettings()
     */
}
