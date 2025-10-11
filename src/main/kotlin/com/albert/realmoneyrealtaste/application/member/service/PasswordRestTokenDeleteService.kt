package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetTokenDeleter
import com.albert.realmoneyrealtaste.application.member.required.PasswordResetTokenRepository
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Transactional
@Service
class PasswordRestTokenDeleteService(
    private val passwordRestTokenRepository: PasswordResetTokenRepository,
) : PasswordResetTokenDeleter {

    override fun delete(passwordResetToken: PasswordResetToken) = passwordRestTokenRepository.delete(passwordResetToken)
}
