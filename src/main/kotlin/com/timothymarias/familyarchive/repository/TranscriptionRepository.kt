package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.entity.Transcription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TranscriptionRepository : JpaRepository<Transcription, Long> {
    fun findByArtifactId(artifactId: Long): Transcription?
}
