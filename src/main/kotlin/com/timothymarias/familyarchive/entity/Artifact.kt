package com.timothymarias.familyarchive.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "artifacts")
class Artifact(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var slug: String,

    @Column(length = 500)
    var title: String? = null,

    @Column(name = "storage_path", nullable = false, length = 1000)
    var storagePath: String,

    @Column(name = "mime_type", nullable = false)
    var mimeType: String,

    @Column(name = "file_size", nullable = false)
    var fileSize: Long,

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    val uploadedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "original_date_string")
    var originalDateString: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToOne(mappedBy = "artifact", cascade = [CascadeType.ALL], orphanRemoval = true)
    var transcription: Transcription? = null,

    @OneToMany(mappedBy = "artifact", cascade = [CascadeType.ALL], orphanRemoval = true)
    var annotations: MutableList<Annotation> = mutableListOf(),

    @OneToMany(mappedBy = "artifact", cascade = [CascadeType.ALL], orphanRemoval = true)
    var commentaries: MutableList<Commentary> = mutableListOf()
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
