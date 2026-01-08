package com.timothymarias.familyarchive.entity

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    private var password: String,

    @Column(nullable = false)
    var name: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) : UserDetails {

    // UserDetails interface implementations
    override fun getAuthorities(): Collection<GrantedAuthority> {
        // For now, all users are admins
        return listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
    }

    override fun getPassword(): String = password

    fun setPassword(password: String) {
        this.password = password
    }

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
