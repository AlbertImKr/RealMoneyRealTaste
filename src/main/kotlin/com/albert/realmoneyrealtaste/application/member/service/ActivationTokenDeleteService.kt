package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenDeleter
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Transactional
@Service
class ActivationTokenDeleteService(
    private val activationTokenRepository: ActivationTokenRepository,
) : ActivationTokenDeleter {

    override fun delete(token: ActivationToken) = activationTokenRepository.delete(token)
}
