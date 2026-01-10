package com.timothymarias.familyarchive.service.image

import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Abstraction for image processing operations.
 * Allows swapping implementations (Thumbnailator, ImageMagick, etc.) without changing API.
 */
interface ImageProcessor {
    /**
     * Resize an image to fit within the specified dimensions, maintaining aspect ratio
     */
    fun resize(input: InputStream, output: OutputStream, width: Int, height: Int, format: String = "jpg")

    /**
     * Resize an image to fit within the specified dimensions, maintaining aspect ratio
     */
    fun resize(inputFile: File, outputFile: File, width: Int, height: Int)

    /**
     * Create a thumbnail with exact dimensions (may crop to fit)
     */
    fun thumbnail(input: InputStream, output: OutputStream, width: Int, height: Int, format: String = "jpg")

    /**
     * Create a thumbnail with exact dimensions (may crop to fit)
     */
    fun thumbnail(inputFile: File, outputFile: File, width: Int, height: Int)

    /**
     * Scale image to exact width, adjusting height to maintain aspect ratio
     */
    fun scaleToWidth(input: InputStream, output: OutputStream, width: Int, format: String = "jpg")

    /**
     * Scale image to exact height, adjusting width to maintain aspect ratio
     */
    fun scaleToHeight(input: InputStream, output: OutputStream, height: Int, format: String = "jpg")

    /**
     * Get image dimensions
     */
    fun getDimensions(input: InputStream): ImageDimensions

    /**
     * Convert image to different format
     */
    fun convert(input: InputStream, output: OutputStream, targetFormat: String)
}

data class ImageDimensions(val width: Int, val height: Int)
