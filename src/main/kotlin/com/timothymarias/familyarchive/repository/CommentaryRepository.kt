package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.entity.Commentary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentaryRepository : JpaRepository<Commentary, Long> {
    fun findByArtifactId(artifactId: Long): List<Commentary>
}
