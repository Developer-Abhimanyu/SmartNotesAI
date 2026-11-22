package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.ImageInput

object ImageInputValidator {

    const val MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024

    private val supportedMimeTypes = setOf(
        "image/jpeg",
        "image/png",
        "image/webp"
    )

    fun validate(image: ImageInput) {

        require(image.bytes.isNotEmpty()) {
            "Image cannot be empty."
        }

        require(image.bytes.size <= MAX_IMAGE_SIZE_BYTES) {
            "Image size must not exceed 10 MB."
        }

        require(image.mimeType in supportedMimeTypes) {
            "Unsupported image format. Use JPEG, PNG, or WebP."
        }
    }
}
