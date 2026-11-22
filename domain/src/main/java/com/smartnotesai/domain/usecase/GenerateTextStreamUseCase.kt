package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.repository.AiRepository
import com.smartnotesai.domain.usecase.validation.AiInputValidator
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GenerateTextStreamUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {

    operator fun invoke(prompt: String): Flow<String> {

        AiInputValidator.validatePrompt(prompt)

        return aiRepository.generateTextStream(prompt)
    }
}