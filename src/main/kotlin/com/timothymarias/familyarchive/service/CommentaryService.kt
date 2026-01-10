package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.entity.Commentary
import com.timothymarias.familyarchive.repository.CommentaryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CommentaryService(
    private val commentaryRepository: CommentaryRepository
) {
    fun findById(id: Long): Commentary? {
        return commentaryRepository.findById(id).orElse(null)
    }

    fun findByArtifactId(artifactId: Long): List<Commentary> {
        return commentaryRepository.findByArtifactId(artifactId)
    }

    fun create(commentary: Commentary): Commentary {
        return commentaryRepository.save(commentary)
    }

    fun update(id: Long, updatedCommentary: Commentary): Commentary? {
        val existing = findById(id) ?: return null

        existing.commentaryText = updatedCommentary.commentaryText
        existing.commentaryType = updatedCommentary.commentaryType

        return commentaryRepository.save(existing)
    }

    fun delete(id: Long): Boolean {
        return if (commentaryRepository.existsById(id)) {
            commentaryRepository.deleteById(id)
            true
        } else {
            false
        }
    }
}
