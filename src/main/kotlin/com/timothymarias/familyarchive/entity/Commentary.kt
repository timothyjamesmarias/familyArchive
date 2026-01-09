package com.timothymarias.familyarchive.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "commentaries")
class Commentary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "artifact_id", nullable = false)
    var artifact: Artifact,

    @Column(name = "commentary_text", nullable = false, columnDefinition = "TEXT")
    var commentaryText: String,

    @Column(name = "commentary_type", length = 50)
    var commentaryType: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
