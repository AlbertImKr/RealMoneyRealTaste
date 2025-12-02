package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.image.provided.ImageReader
import com.albert.realmoneyrealtaste.application.member.exception.MemberVerifyException
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberVerify
import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class MemberVerifyService(
    private val passwordEncoder: PasswordEncoder,
    private val memberReader: MemberReader,
    private val imageReader: ImageReader,
) : MemberVerify {

    companion object {
        private const val ERROR_MEMBER_VERIFY = "회원 인증에 실패했습니다"
    }

    override fun verify(
        email: Email,
        password: RawPassword,
    ): MemberPrincipal {
        try {
            val member = memberReader.readMemberByEmail(email)

            val imageUrl = member.detail.imageId?.let { imageId ->
                imageReader.getImageUrl(imageId, member.requireId())
            }

            require(member.verifyPassword(password, passwordEncoder)) {}

            return MemberPrincipal.from(member, imageUrl)
        } catch (e: IllegalArgumentException) {
            throw MemberVerifyException(ERROR_MEMBER_VERIFY)
        }
    }
}
