package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.entity.Artifact
import com.timothymarias.familyarchive.repository.ArtifactRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ArtifactService(
    private val artifactRepository: ArtifactRepository
) {
    fun findAll(pageable: Pageable): Page<Artifact> {
        return artifactRepository.findAll(pageable)
    }

    fun findAll(): List<Artifact> {
        return artifactRepository.findAll()
    }

    fun findById(id: Long): Artifact? {
        return artifactRepository.findById(id).orElse(null)
    }

    fun findBySlug(slug: String): Artifact? {
        return artifactRepository.findBySlug(slug)
    }

    fun create(artifact: Artifact): Artifact {
        return artifactRepository.save(artifact)
    }

    fun update(id: Long, updatedArtifact: Artifact): Artifact? {
        val existing = findById(id) ?: return null

        existing.slug = updatedArtifact.slug
        existing.title = updatedArtifact.title
        existing.storagePath = updatedArtifact.storagePath
        existing.mimeType = updatedArtifact.mimeType
        existing.fileSize = updatedArtifact.fileSize
        existing.originalDateString = updatedArtifact.originalDateString

        return artifactRepository.save(existing)
    }

    fun delete(id: Long): Boolean {
        return if (artifactRepository.existsById(id)) {
            artifactRepository.deleteById(id)
            true
        } else {
            false
        }
    }
}
