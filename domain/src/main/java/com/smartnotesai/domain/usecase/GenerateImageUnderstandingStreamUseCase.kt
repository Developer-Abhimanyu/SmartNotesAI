package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.model.ImageInput
import com.smartnotesai.domain.repository.AiRepository
import com.smartnotesai.domain.usecase.validation.AiInputValidator
import com.smartnotesai.domain.usecase.validation.ImageInputValidator
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GenerateImageUnderstandingStreamUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {

    operator fun invoke(
        image: ImageInput,
        prompt: String
    ): Flow<String> {

        ImageInputValidator.validate(image)
        AiInputValidator.validatePrompt(prompt)

        return aiRepository.generateImageUnderstandingStream(
            image = image,
            prompt = prompt
        )
    }
}
