package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.model.MeetingSummary
import com.smartnotesai.domain.repository.MeetingSummaryRepository
import com.smartnotesai.domain.usecase.validation.AiInputValidator
import javax.inject.Inject

class GenerateMeetingSummaryUseCase @Inject constructor(
    private val meetingSummaryRepository: MeetingSummaryRepository
) {

    suspend operator fun invoke(
        meetingNotes: String
    ): MeetingSummary {

        AiInputValidator.validateMeetingNotes(meetingNotes)

        return meetingSummaryRepository.generateMeetingSummary(
            meetingNotes
        )
    }
}
