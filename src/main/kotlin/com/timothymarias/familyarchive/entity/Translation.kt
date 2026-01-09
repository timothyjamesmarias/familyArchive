package com.timothymarias.familyarchive.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "translations")
class Translation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "transcription_id", nullable = false)
    var transcription: Transcription,

    @Column(name = "translated_text", nullable = false, columnDefinition = "TEXT")
    var translatedText: String,

    @Column(name = "target_language", nullable = false, length = 10)
    var targetLanguage: String,

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
