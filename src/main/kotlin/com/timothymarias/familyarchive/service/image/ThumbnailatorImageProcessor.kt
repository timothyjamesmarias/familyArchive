package com.timothymarias.familyarchive.service.image

import net.coobird.thumbnailator.Thumbnails
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.imageio.ImageIO

@Service
class ThumbnailatorImageProcessor : ImageProcessor {

    override fun resize(input: InputStream, output: OutputStream, width: Int, height: Int, format: String) {
        Thumbnails.of(input)
            .size(width, height)
            .outputFormat(format)
            .toOutputStream(output)
    }

    override fun resize(inputFile: File, outputFile: File, width: Int, height: Int) {
        Thumbnails.of(inputFile)
            .size(width, height)
            .toFile(outputFile)
    }

    override fun thumbnail(input: InputStream, output: OutputStream, width: Int, height: Int, format: String) {
        // Use crop to create exact dimensions
        Thumbnails.of(input)
            .size(width, height)
            .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
            .outputFormat(format)
            .toOutputStream(output)
    }

    override fun thumbnail(inputFile: File, outputFile: File, width: Int, height: Int) {
        Thumbnails.of(inputFile)
            .size(width, height)
            .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
            .toFile(outputFile)
    }

    override fun scaleToWidth(input: InputStream, output: OutputStream, width: Int, format: String) {
        Thumbnails.of(input)
            .width(width)
            .outputFormat(format)
            .toOutputStream(output)
    }

    override fun scaleToHeight(input: InputStream, output: OutputStream, height: Int, format: String) {
        Thumbnails.of(input)
            .height(height)
            .outputFormat(format)
            .toOutputStream(output)
    }

    override fun getDimensions(input: InputStream): ImageDimensions {
        val bufferedImage = ImageIO.read(input)
        return ImageDimensions(bufferedImage.width, bufferedImage.height)
    }

    override fun convert(input: InputStream, output: OutputStream, targetFormat: String) {
        Thumbnails.of(input)
            .scale(1.0) // Keep original size
            .outputFormat(targetFormat)
            .toOutputStream(output)
    }
}
