package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.ImageInput
import com.smartnotesai.domain.repository.AiRepository
import com.smartnotesai.domain.usecase.CorrectGrammarUseCase
import com.smartnotesai.domain.usecase.GenerateTextUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CorrectGrammarUseCaseTest {

    private class FakeAiRepository : AiRepository {

        var wasCalled = false

        override suspend fun generateText(prompt: String): String {
            wasCalled = true
            return "This is corrected text."
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
    fun `valid text generates corrected text`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = CorrectGrammarUseCase(
            GenerateTextUseCase(repository)
        )

        val result = useCase("this is a test")

        assertEquals("This is corrected text.", result)
        assertEquals(true, repository.wasCalled)
    }

    @Test
    fun `blank text does not call repository`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = CorrectGrammarUseCase(
            GenerateTextUseCase(repository)
        )

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(" ")
            }
        }

        assertEquals(false, repository.wasCalled)
    }

    @Test
    fun `oversized text does not call repository`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = CorrectGrammarUseCase(
            GenerateTextUseCase(repository)
        )

        val oversizedText = "a".repeat(20_001)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(oversizedText)
            }
        }

        assertEquals(false, repository.wasCalled)
    }
}
