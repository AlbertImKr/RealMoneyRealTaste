package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenReader
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.exceptions.InvalidActivationTokenException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class ActivationTokenReadService(
    private val activationTokenRepository: ActivationTokenRepository,
) : ActivationTokenReader {

    override fun findByToken(token: String): ActivationToken {
        return activationTokenRepository.findByToken(token) ?: throw InvalidActivationTokenException()
    }
}
