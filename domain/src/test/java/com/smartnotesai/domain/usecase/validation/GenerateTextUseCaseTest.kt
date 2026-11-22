package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.ImageInput
import com.smartnotesai.domain.repository.AiRepository
import com.smartnotesai.domain.usecase.GenerateTextUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GenerateTextUseCaseTest {

    private class FakeAiRepository : AiRepository {

        var wasCalled = false

        override suspend fun generateText(prompt: String): String {
            wasCalled = true
            return "Generated response"
        }

        override fun generateTextStream(prompt: String): Flow<String> {
            return flowOf("Generated ", "streaming ", "response")
        }

        override fun generateImageUnderstandingStream(
            image: ImageInput,
            prompt: String
        ): Flow<String> {
            return flowOf("Generated image response")
        }
    }

    @Test
    fun `valid prompt calls repository`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = GenerateTextUseCase(repository)

        val result = useCase("Summarize this note")

        assertEquals("Generated response", result)
        assertEquals(true, repository.wasCalled)
    }

    @Test
    fun `blank prompt does not call repository`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = GenerateTextUseCase(repository)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(" ")
            }
        }

        assertEquals(false, repository.wasCalled)
    }

    @Test
    fun `oversized prompt does not call repository`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = GenerateTextUseCase(repository)

        val oversizedPrompt = "a".repeat(
            25_001
        )

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(oversizedPrompt)
            }
        }

        assertEquals(false, repository.wasCalled)
    }
}
