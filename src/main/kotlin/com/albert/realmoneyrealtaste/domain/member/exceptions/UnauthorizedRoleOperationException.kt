package com.albert.realmoneyrealtaste.domain.member.exceptions

/**
 * 권한 작업에 대한 권한이 없는 경우
 */
class UnauthorizedRoleOperationException(message: String) : MemberDomainException(message)
