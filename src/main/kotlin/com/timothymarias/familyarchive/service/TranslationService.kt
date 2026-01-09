package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.entity.Translation
import com.timothymarias.familyarchive.repository.TranslationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TranslationService(
    private val translationRepository: TranslationRepository
) {
    fun findById(id: Long): Translation? {
        return translationRepository.findById(id).orElse(null)
    }

    fun findByTranscriptionId(transcriptionId: Long): List<Translation> {
        return translationRepository.findByTranscriptionId(transcriptionId)
    }

    fun create(translation: Translation): Translation {
        return translationRepository.save(translation)
    }

    fun update(id: Long, updatedTranslation: Translation): Translation? {
        val existing = findById(id) ?: return null

        existing.translatedText = updatedTranslation.translatedText
        existing.targetLanguage = updatedTranslation.targetLanguage

        return translationRepository.save(existing)
    }

    fun delete(id: Long): Boolean {
        return if (translationRepository.existsById(id)) {
            translationRepository.deleteById(id)
            true
        } else {
            false
        }
    }
}
