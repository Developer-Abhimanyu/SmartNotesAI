package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.model.ChatMessage
import com.smartnotesai.domain.model.ImageInput
import com.smartnotesai.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GenerateImageUnderstandingStreamUseCaseTest {

    private class FakeAiRepository : AiRepository {

        override suspend fun generateText(prompt: String): String {
            return "Generated response"
        }

        override fun generateTextStream(prompt: String): Flow<String> {
            return flowOf("Generated response")
        }

        override fun generateChatResponseStream(
            history: List<ChatMessage>,
            prompt: String
        ): Flow<String> {
            return flowOf("Chat response")
        }

        override fun generateImageUnderstandingStream(
            image: ImageInput,
            prompt: String
        ): Flow<String> {
            return flowOf("Image analyzed", "Successfully")
        }
    }

    @Test
    fun `valid image and prompt return streaming response`() = runBlocking {
        val useCase = GenerateImageUnderstandingStreamUseCase(
            FakeAiRepository()
        )

        val result = useCase(
            image = ImageInput(
                bytes = byteArrayOf(1, 2, 3),
                mimeType = "image/jpeg"
            ),
            prompt = "Describe this image"
        ).toList()

        assertEquals(
            listOf("Image analyzed", "Successfully"),
            result
        )
    }

    @Test
    fun `empty image is rejected`() {
        val useCase = GenerateImageUnderstandingStreamUseCase(
            FakeAiRepository()
        )

        assertThrows(IllegalArgumentException::class.java) {
            useCase(
                ImageInput(byteArrayOf(), "image/jpeg"),
                "Describe this image"
            )
        }
    }

    @Test
    fun `unsupported image format is rejected`() {
        val useCase = GenerateImageUnderstandingStreamUseCase(
            FakeAiRepository()
        )

        assertThrows(IllegalArgumentException::class.java) {
            useCase(
                ImageInput(byteArrayOf(1, 2), "image/gif"),
                "Describe this image"
            )
        }
    }

    @Test
    fun `blank prompt is rejected`() {
        val useCase = GenerateImageUnderstandingStreamUseCase(
            FakeAiRepository()
        )

        assertThrows(IllegalArgumentException::class.java) {
            useCase(
                ImageInput(byteArrayOf(1, 2, 3), "image/jpeg"),
                "   "
            )
        }
    }
}
