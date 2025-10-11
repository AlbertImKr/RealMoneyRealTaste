package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.exception.InvalidPasswordResetTokenException
import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetTokenReader
import com.albert.realmoneyrealtaste.application.member.required.PasswordResetTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class PasswordRestTokenReadService(
    private val passwordRestTokenRepository: PasswordResetTokenRepository,
) : PasswordResetTokenReader {

    override fun findByToken(token: String) = passwordRestTokenRepository.findByToken(token)
        ?: throw InvalidPasswordResetTokenException("유효하지 않은 비밀번호 재설정 토큰입니다. token=$token")

    override fun findByMemberId(memberId: Long) = passwordRestTokenRepository.findByMemberId(memberId)
        ?: throw InvalidPasswordResetTokenException("해당 회원의 비밀번호 재설정 토큰이 존재하지 않습니다. memberId=$memberId")
}
