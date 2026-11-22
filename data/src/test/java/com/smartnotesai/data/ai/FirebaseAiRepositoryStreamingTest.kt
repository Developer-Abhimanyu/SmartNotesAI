package com.smartnotesai.data.ai

import com.smartnotesai.data.ai.streaming.FakeGeminiStreamingClient
import com.smartnotesai.domain.model.AiError
import com.smartnotesai.domain.model.ImageInput
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class FirebaseAiRepositoryStreamingTest {

    @Test
    fun `stream emits all chunks in correct order`() = runBlocking {
        val client = FakeGeminiStreamingClient(
            chunks = listOf("Hello ", "from ", "Gemini!")
        )

        val repository = FirebaseAiRepository(client)

        val result = repository.generateTextStream("Say hello").toList()

        assertEquals(
            listOf("Hello ", "from ", "Gemini!"),
            result
        )
    }

    @Test
    fun `empty stream emits no chunks`() = runBlocking {
        val client = FakeGeminiStreamingClient()
        val repository = FirebaseAiRepository(client)

        val result = repository.generateTextStream("Say hello").toList()

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `network failure is mapped to AiError Network`() = runBlocking {
        val client = FakeGeminiStreamingClient(
            chunks = listOf("Hello"),
            exception = IOException("Network failure")
        )

        val repository = FirebaseAiRepository(client)

        val exception = assertThrows(AiError.Network::class.java) {
            runBlocking {
                repository.generateTextStream("Say hello").toList()
            }
        }

        assertEquals("Network failure", exception.cause?.message)
    }

    @Test
    fun `network failure after partial response preserves emitted chunks`() =
        runBlocking {
            val client = FakeGeminiStreamingClient(
                chunks = listOf("Hello "),
                exception = IOException("Network failure")
            )

            val repository = FirebaseAiRepository(client)
            val receivedChunks = mutableListOf<String>()

            val exception = assertThrows(AiError.Network::class.java) {
                runBlocking {
                    repository.generateTextStream("Say hello").collect { chunk ->
                        receivedChunks.add(chunk)
                    }
                }
            }

            assertEquals(listOf("Hello "), receivedChunks)
            assertEquals("Network failure", exception.cause?.message)
        }

    @Test
    fun `stream cancellation is propagated without error mapping`() =
        runBlocking {
            val client = FakeGeminiStreamingClient(
                chunks = listOf("Hello "),
                exception = CancellationException("Request cancelled")
            )

            val repository = FirebaseAiRepository(client)
            val receivedChunks = mutableListOf<String>()

            val exception = assertThrows(CancellationException::class.java) {
                runBlocking {
                    repository.generateTextStream("Say hello").collect { chunk ->
                        receivedChunks.add(chunk)
                    }
                }
            }

            assertEquals(listOf("Hello "), receivedChunks)
            assertEquals("Request cancelled", exception.message)
        }

    // Image streaming tests

    @Test
    fun `image stream emits all chunks in correct order`() = runBlocking {
        val client = FakeGeminiStreamingClient(
            imageChunks = listOf(
                "This image ",
                "contains handwritten ",
                "notes."
            )
        )

        val repository = FirebaseAiRepository(client)

        val image = ImageInput(
            bytes = byteArrayOf(1, 2, 3, 4),
            mimeType = "image/jpeg"
        )

        val result = repository
            .generateImageUnderstandingStream(
                image = image,
                prompt = "Describe this image"
            )
            .toList()

        assertEquals(
            listOf(
                "This image ",
                "contains handwritten ",
                "notes."
            ),
            result
        )
    }

    @Test
    fun `empty image stream emits no chunks`() = runBlocking {
        val client = FakeGeminiStreamingClient()
        val repository = FirebaseAiRepository(client)

        val image = ImageInput(
            bytes = byteArrayOf(1, 2, 3),
            mimeType = "image/png"
        )

        val result = repository
            .generateImageUnderstandingStream(
                image = image,
                prompt = "Describe this image"
            )
            .toList()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `image network failure is mapped to AiError Network`() =
        runBlocking {
            val client = FakeGeminiStreamingClient(
                imageChunks = listOf("Analyzing image"),
                exception = IOException("Image network failure")
            )

            val repository = FirebaseAiRepository(client)

            val image = ImageInput(
                bytes = byteArrayOf(1, 2, 3),
                mimeType = "image/jpeg"
            )

            val exception = assertThrows(AiError.Network::class.java) {
                runBlocking {
                    repository.generateImageUnderstandingStream(
                        image,
                        "Describe this image"
                    ).toList()
                }
            }

            assertEquals(
                "Image network failure",
                exception.cause?.message
            )
        }

    @Test
    fun `image stream cancellation is propagated without error mapping`() =
        runBlocking {
            val client = FakeGeminiStreamingClient(
                imageChunks = listOf("Analyzing"),
                exception = CancellationException("Image request cancelled")
            )

            val repository = FirebaseAiRepository(client)

            val image = ImageInput(
                bytes = byteArrayOf(1, 2, 3),
                mimeType = "image/jpeg"
            )

            val exception = assertThrows(CancellationException::class.java) {
                runBlocking {
                    repository.generateImageUnderstandingStream(
                        image,
                        "Describe this image"
                    ).toList()
                }
            }

            assertEquals("Image request cancelled", exception.message)
        }

    @Test
    fun `image bytes and mime type are passed to streaming client`() =
        runBlocking {
            val client = FakeGeminiStreamingClient(
                imageChunks = listOf("Image analyzed")
            )

            val repository = FirebaseAiRepository(client)

            val imageBytes = byteArrayOf(10, 20, 30, 40)
            val image = ImageInput(
                bytes = imageBytes,
                mimeType = "image/png"
            )

            val prompt = "Extract text from this image"

            repository.generateImageUnderstandingStream(
                image = image,
                prompt = prompt
            ).toList()

            assertArrayEquals(imageBytes, client.receivedImage?.bytes)
            assertEquals("image/png", client.receivedImage?.mimeType)
            assertEquals(prompt, client.receivedPrompt)
        }
}
