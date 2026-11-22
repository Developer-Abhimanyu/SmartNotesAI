package com.smartnotesai.domain.repository

import com.smartnotesai.domain.model.ImageInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AiRepositoryStreamingTest {

    private class FakeAiRepository : AiRepository {

        override suspend fun generateText(prompt: String): String {
            return "Generated response"
        }

        override fun generateTextStream(prompt: String): Flow<String> = flow {
            emit("Hello ")
            emit("from ")
            emit("Gemini!")
        }

        override fun generateImageUnderstandingStream(
            image: ImageInput,
            prompt: String
        ): Flow<String> {
            return flowOf("Generated image response")
        }
    }

    @Test
    fun `stream emits chunks in correct order`() = runBlocking {
        val repository = FakeAiRepository()
        val receivedChunks = mutableListOf<String>()

        repository.generateTextStream("Say hello").collect { chunk ->
            receivedChunks.add(chunk)
        }

        assertEquals(
            listOf("Hello ", "from ", "Gemini!"),
            receivedChunks
        )
    }
}