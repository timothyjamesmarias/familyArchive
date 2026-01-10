package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.entity.Annotation
import com.timothymarias.familyarchive.repository.AnnotationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AnnotationService(
    private val annotationRepository: AnnotationRepository
) {
    fun findById(id: Long): Annotation? {
        return annotationRepository.findById(id).orElse(null)
    }

    fun findByArtifactId(artifactId: Long): List<Annotation> {
        return annotationRepository.findByArtifactId(artifactId)
    }

    fun create(annotation: Annotation): Annotation {
        return annotationRepository.save(annotation)
    }

    fun update(id: Long, updatedAnnotation: Annotation): Annotation? {
        val existing = findById(id) ?: return null

        existing.annotationText = updatedAnnotation.annotationText
        existing.xCoord = updatedAnnotation.xCoord
        existing.yCoord = updatedAnnotation.yCoord

        return annotationRepository.save(existing)
    }

    fun delete(id: Long): Boolean {
        return if (annotationRepository.existsById(id)) {
            annotationRepository.deleteById(id)
            true
        } else {
            false
        }
    }
}
