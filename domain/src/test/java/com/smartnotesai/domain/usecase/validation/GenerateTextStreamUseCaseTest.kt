package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.ImageInput
import com.smartnotesai.domain.repository.AiRepository
import com.smartnotesai.domain.usecase.GenerateTextStreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class GenerateTextStreamUseCaseTest {

    private class FakeAiRepository : AiRepository {

        var wasCalled = false

        override suspend fun generateText(prompt: String): String {
            return "Generated response"
        }

        override fun generateTextStream(prompt: String): Flow<String> {
            wasCalled = true

            return flowOf(
                "Hello ",
                "from ",
                "Gemini!"
            )
        }

        override fun generateImageUnderstandingStream(
            image: ImageInput,
            prompt: String
        ): Flow<String> {
            return flowOf("Generated image response")
        }
    }

    @Test
    fun `valid prompt returns streaming chunks`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = GenerateTextStreamUseCase(repository)

        val result = useCase("Say hello").toList()

        assertEquals(
            listOf("Hello ", "from ", "Gemini!"),
            result
        )
        assertEquals(true, repository.wasCalled)
    }

    @Test
    fun `blank prompt throws exception`() {
        val repository = FakeAiRepository()
        val useCase = GenerateTextStreamUseCase(repository)

        assertThrows(IllegalArgumentException::class.java) {
            useCase(" ")
        }

        assertFalse(repository.wasCalled)
    }

    @Test
    fun `oversized prompt throws exception`() {
        val repository = FakeAiRepository()
        val useCase = GenerateTextStreamUseCase(repository)

        val oversizedPrompt = "a".repeat(25_001)

        assertThrows(IllegalArgumentException::class.java) {
            useCase(oversizedPrompt)
        }

        assertFalse(repository.wasCalled)
    }
}