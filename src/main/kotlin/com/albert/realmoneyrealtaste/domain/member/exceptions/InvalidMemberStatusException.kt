package com.albert.realmoneyrealtaste.domain.member.exceptions

/**
 * 유효하지 않은 회원 상태에서 작업을 시도한 경우
 */
sealed class InvalidMemberStatusException(message: String) : MemberDomainException(message) {
    class NotActive(message: String) : InvalidMemberStatusException(message)
    class NotPending(message: String) : InvalidMemberStatusException(message)
}
