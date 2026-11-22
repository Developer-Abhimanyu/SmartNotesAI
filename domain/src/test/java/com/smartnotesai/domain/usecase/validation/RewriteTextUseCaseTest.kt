package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.ImageInput
import com.smartnotesai.domain.repository.AiRepository
import com.smartnotesai.domain.usecase.GenerateTextUseCase
import com.smartnotesai.domain.usecase.RewriteTextUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RewriteTextUseCaseTest {

    private class FakeAiRepository : AiRepository {

        var wasCalled = false

        override suspend fun generateText(prompt: String): String {
            wasCalled = true
            return "Rewritten text"
        }

        override fun generateTextStream(prompt: String): Flow<String> {
            return flowOf("Generated response")
        }

        override fun generateImageUnderstandingStream(
            image: ImageInput,
            prompt: String
        ): Flow<String> {
            return flowOf("Generated image response")
        }
    }

    @Test
    fun `valid text and supported style generate rewritten text`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = RewriteTextUseCase(
            GenerateTextUseCase(repository)
        )

        val result = useCase("hello world", "Professional")

        assertEquals("Rewritten text", result)
        assertEquals(true, repository.wasCalled)
    }

    @Test
    fun `blank text does not call repository`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = RewriteTextUseCase(
            GenerateTextUseCase(repository)
        )

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(" ", "Professional")
            }
        }

        assertEquals(false, repository.wasCalled)
    }

    @Test
    fun `oversized text does not call repository`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = RewriteTextUseCase(
            GenerateTextUseCase(repository)
        )

        val oversizedText = "a".repeat(20_001)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(oversizedText, "Professional")
            }
        }

        assertEquals(false, repository.wasCalled)
    }

    @Test
    fun `unsupported style does not call repository`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = RewriteTextUseCase(
            GenerateTextUseCase(repository)
        )

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase("hello world", "Funny")
            }
        }

        assertEquals(false, repository.wasCalled)
    }
}
