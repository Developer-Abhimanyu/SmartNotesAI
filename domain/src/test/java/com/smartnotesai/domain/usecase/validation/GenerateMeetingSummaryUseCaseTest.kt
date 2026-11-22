package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.MeetingSummary
import com.smartnotesai.domain.repository.MeetingSummaryRepository
import com.smartnotesai.domain.usecase.GenerateMeetingSummaryUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GenerateMeetingSummaryUseCaseTest {

    private class FakeMeetingSummaryRepository :
        MeetingSummaryRepository {

        var wasCalled = false

        override suspend fun generateMeetingSummary(
            meetingNotes: String
        ): MeetingSummary {
            wasCalled = true

            return MeetingSummary(
                summary = "Project discussion",
                decisions = listOf("Launch next month"),
                actionItems = emptyList()
            )
        }
    }

    @Test
    fun `valid meeting notes generate summary`() = runBlocking {
        val repository = FakeMeetingSummaryRepository()
        val useCase = GenerateMeetingSummaryUseCase(repository)

        val result = useCase("Discussed project launch")

        assertEquals("Project discussion", result.summary)
        assertEquals(true, repository.wasCalled)
    }

    @Test
    fun `blank meeting notes do not call repository`() = runBlocking {
        val repository = FakeMeetingSummaryRepository()
        val useCase = GenerateMeetingSummaryUseCase(repository)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(" ")
            }
        }

        assertEquals(false, repository.wasCalled)
    }

    @Test
    fun `oversized meeting notes do not call repository`() = runBlocking {
        val repository = FakeMeetingSummaryRepository()
        val useCase = GenerateMeetingSummaryUseCase(repository)

        val oversizedNotes = "a".repeat(20_001)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(oversizedNotes)
            }
        }

        assertEquals(false, repository.wasCalled)
    }
}
