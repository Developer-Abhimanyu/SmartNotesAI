package com.smartnotesai.domain.usecase.prompt

interface PromptTemplate {

    fun buildPrompt(): String
}