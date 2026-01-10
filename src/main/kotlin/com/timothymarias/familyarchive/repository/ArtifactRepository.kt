package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.entity.Artifact
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArtifactRepository : JpaRepository<Artifact, Long> {
    fun findBySlug(slug: String): Artifact?
}
