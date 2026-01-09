package com.timothymarias.familyarchive.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "transcriptions")
class Transcription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne
    @JoinColumn(name = "artifact_id", nullable = false)
    var artifact: Artifact,

    @Column(name = "transcription_text", nullable = false, columnDefinition = "TEXT")
    var transcriptionText: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "transcription", cascade = [CascadeType.ALL], orphanRemoval = true)
    var translations: MutableList<Translation> = mutableListOf()
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
