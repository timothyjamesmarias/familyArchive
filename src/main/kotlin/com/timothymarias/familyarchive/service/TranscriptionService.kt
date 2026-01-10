package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.entity.Transcription
import com.timothymarias.familyarchive.repository.TranscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TranscriptionService(
    private val transcriptionRepository: TranscriptionRepository
) {
    fun findById(id: Long): Transcription? {
        return transcriptionRepository.findById(id).orElse(null)
    }

    fun findByArtifactId(artifactId: Long): Transcription? {
        return transcriptionRepository.findByArtifactId(artifactId)
    }

    fun create(transcription: Transcription): Transcription {
        return transcriptionRepository.save(transcription)
    }

    fun update(id: Long, updatedTranscription: Transcription): Transcription? {
        val existing = findById(id) ?: return null

        existing.transcriptionText = updatedTranscription.transcriptionText

        return transcriptionRepository.save(existing)
    }

    fun delete(id: Long): Boolean {
        return if (transcriptionRepository.existsById(id)) {
            transcriptionRepository.deleteById(id)
            true
        } else {
            false
        }
    }
}
