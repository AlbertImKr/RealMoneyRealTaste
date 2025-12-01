package com.albert.realmoneyrealtaste.application.member.exception

/**
 * 이미 사용 중인 프로필 주소에 대해 발생하는 예외
 *
 * @param message 예외 메시지
 */
class DuplicateProfileAddressException(message: String) : MemberApplicationException(message)
