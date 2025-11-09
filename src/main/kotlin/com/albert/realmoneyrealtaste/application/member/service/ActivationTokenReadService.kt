package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.exception.ActivationTokenNotFoundException
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenReader
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class ActivationTokenReadService(
    private val activationTokenRepository: ActivationTokenRepository,
) : ActivationTokenReader {

    companion object {
        const val ERROR_ACTIVATION_TOKEN_NOT_FOUND = "활성화 토큰을 찾을 수 없습니다."
    }

    override fun findByToken(token: String): ActivationToken {
        return activationTokenRepository.findByToken(token)
            ?: throw ActivationTokenNotFoundException(ERROR_ACTIVATION_TOKEN_NOT_FOUND)
    }
}
