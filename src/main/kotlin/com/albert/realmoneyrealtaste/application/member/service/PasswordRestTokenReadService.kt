package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.exception.InvalidPasswordResetTokenException
import com.albert.realmoneyrealtaste.application.member.exception.PasswordResetTokenNotFoundException
import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetTokenReader
import com.albert.realmoneyrealtaste.application.member.required.PasswordResetTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class PasswordRestTokenReadService(
    private val passwordRestTokenRepository: PasswordResetTokenRepository,
) : PasswordResetTokenReader {

    companion object {
        private const val ERROR_INVALID_PASSWORD_RESET_TOKEN = "유효하지 않은 비밀번호 재설정 토큰입니다."
        private const val ERROR_PASSWORD_RESET_TOKEN_NOT_FOUND = "비밀번호 재설정 토큰을 찾을 수 없습니다."
    }

    override fun findByToken(token: String) = passwordRestTokenRepository.findByToken(token)
        ?: throw InvalidPasswordResetTokenException(ERROR_INVALID_PASSWORD_RESET_TOKEN)

    override fun findByMemberId(memberId: Long) = passwordRestTokenRepository.findByMemberId(memberId)
        ?: throw PasswordResetTokenNotFoundException(ERROR_PASSWORD_RESET_TOKEN_NOT_FOUND)

    override fun findByMemberIdOrNull(memberId: Long) = passwordRestTokenRepository.findByMemberId(memberId)
}
