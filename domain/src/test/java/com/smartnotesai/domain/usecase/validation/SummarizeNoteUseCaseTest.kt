package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.ImageInput
import com.smartnotesai.domain.repository.AiRepository
import com.smartnotesai.domain.usecase.GenerateTextUseCase
import com.smartnotesai.domain.usecase.SummarizeNoteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SummarizeNoteUseCaseTest {

    private class FakeAiRepository : AiRepository {

        var wasCalled = false
        var receivedPrompt: String? = null

        override suspend fun generateText(prompt: String): String {
            wasCalled = true
            receivedPrompt = prompt
            return "Generated summary"
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
    fun `valid note content generates summary`() = runBlocking {
        val repository = FakeAiRepository()
        val generateTextUseCase = GenerateTextUseCase(repository)
        val useCase = SummarizeNoteUseCase(generateTextUseCase)

        val result = useCase("Discussed project milestones")

        assertEquals("Generated summary", result)
        assertEquals(true, repository.wasCalled)
        assertEquals(true, repository.receivedPrompt?.contains(
            "Discussed project milestones"
        ))
    }

    @Test
    fun `blank note content does not call repository`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = SummarizeNoteUseCase(
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
    fun `oversized note content does not call repository`() = runBlocking {
        val repository = FakeAiRepository()
        val useCase = SummarizeNoteUseCase(
            GenerateTextUseCase(repository)
        )

        val oversizedContent = "a".repeat(20_001)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(oversizedContent)
            }
        }

        assertEquals(false, repository.wasCalled)
    }
}
