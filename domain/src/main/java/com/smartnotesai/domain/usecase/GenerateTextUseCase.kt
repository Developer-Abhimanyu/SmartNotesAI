package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.repository.AiRepository
import com.smartnotesai.domain.usecase.validation.AiInputValidator
import javax.inject.Inject

class GenerateTextUseCase @Inject constructor(private val aiRepository: AiRepository) {
    suspend operator fun invoke(prompt: String): String {

        AiInputValidator.validatePrompt(prompt)
        return aiRepository.generateText(prompt)
    }
}