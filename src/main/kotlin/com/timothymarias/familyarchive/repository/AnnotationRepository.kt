package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.entity.Annotation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AnnotationRepository : JpaRepository<Annotation, Long> {
    fun findByArtifactId(artifactId: Long): List<Annotation>
}
