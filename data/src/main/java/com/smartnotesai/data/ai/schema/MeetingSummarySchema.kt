package com.smartnotesai.data.ai.schema

import com.google.firebase.ai.type.Schema

object MeetingSummarySchema {

    val schema = Schema.obj(
        mapOf(
            "summary" to Schema.string(),

            "decisions" to Schema.array(
                Schema.string()
            ),

            "actionItems" to Schema.array(
                Schema.obj(
                    mapOf(
                        "task" to Schema.string(),
                        "assignedTo" to Schema.string(),
                        "deadline" to Schema.string(),
                        "priority" to Schema.string()
                    ),
                    optionalProperties = listOf(
                        "assignedTo",
                        "deadline",
                        "priority"
                    )
                )
            )
        )
    )
}