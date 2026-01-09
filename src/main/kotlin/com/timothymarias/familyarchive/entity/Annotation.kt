package com.timothymarias.familyarchive.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "annotations")
class Annotation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "artifact_id", nullable = false)
    var artifact: Artifact,

    @Column(name = "annotation_text", nullable = false, columnDefinition = "TEXT")
    var annotationText: String,

    @Column(name = "x_coord", precision = 10, scale = 6)
    var xCoord: BigDecimal? = null,

    @Column(name = "y_coord", precision = 10, scale = 6)
    var yCoord: BigDecimal? = null,

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
