package com.smartnotesai.feature.chat.image

import android.content.Context
import android.net.Uri
import com.smartnotesai.domain.model.ImageInput
import com.smartnotesai.domain.usecase.validation.ImageInputValidator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject

class ImageUriProcessor @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun process(uri: Uri): ImageInput {

        val contentResolver = context.contentResolver

        val mimeType = contentResolver.getType(uri)
            ?: throw IllegalArgumentException(
                "Unable to determine image format."
            )

        val imageBytes = contentResolver.openInputStream(uri)?.use { input ->

            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8 * 1024)
            var totalBytes = 0

            while (true) {
                val bytesRead = input.read(buffer)

                if (bytesRead == -1) break

                totalBytes += bytesRead

                require(
                    totalBytes <= ImageInputValidator.MAX_IMAGE_SIZE_BYTES
                ) {
                    "Image size must not exceed 10 MB."
                }

                output.write(buffer, 0, bytesRead)
            }

            output.toByteArray()

        } ?: throw IOException("Unable to read selected image.")

        val imageInput = ImageInput(
            bytes = imageBytes,
            mimeType = mimeType
        )

        ImageInputValidator.validate(imageInput)

        return imageInput
    }
}
