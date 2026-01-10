package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.entity.Translation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TranslationRepository : JpaRepository<Translation, Long> {
    fun findByTranscriptionId(transcriptionId: Long): List<Translation>
}
