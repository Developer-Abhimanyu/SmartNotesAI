package com.smartnotesai.domain.repository

import com.smartnotesai.domain.model.MeetingSummary

interface MeetingSummaryRepository {

    suspend fun generateMeetingSummary(
        meetingNotes: String
    ): MeetingSummary
}
