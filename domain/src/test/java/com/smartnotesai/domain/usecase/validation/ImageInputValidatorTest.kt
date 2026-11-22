package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.ImageInput
import org.junit.Assert.assertThrows
import org.junit.Test

class ImageInputValidatorTest {

    @Test
    fun `valid JPEG image passes validation`() {
        ImageInputValidator.validate(
            ImageInput(
                bytes = byteArrayOf(1, 2, 3),
                mimeType = "image/jpeg"
            )
        )
    }

    @Test
    fun `valid PNG image passes validation`() {
        ImageInputValidator.validate(
            ImageInput(
                bytes = byteArrayOf(1, 2, 3),
                mimeType = "image/png"
            )
        )
    }

    @Test
    fun `valid WebP image passes validation`() {
        ImageInputValidator.validate(
            ImageInput(
                bytes = byteArrayOf(1, 2, 3),
                mimeType = "image/webp"
            )
        )
    }

    @Test
    fun `empty image is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageInputValidator.validate(
                ImageInput(
                    bytes = byteArrayOf(),
                    mimeType = "image/jpeg"
                )
            )
        }
    }

    @Test
    fun `image larger than 10 MB is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageInputValidator.validate(
                ImageInput(
                    bytes = ByteArray(10 * 1024 * 1024 + 1),
                    mimeType = "image/jpeg"
                )
            )
        }
    }

    @Test
    fun `unsupported MIME type is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageInputValidator.validate(
                ImageInput(
                    bytes = byteArrayOf(1, 2, 3),
                    mimeType = "image/gif"
                )
            )
        }
    }
}
