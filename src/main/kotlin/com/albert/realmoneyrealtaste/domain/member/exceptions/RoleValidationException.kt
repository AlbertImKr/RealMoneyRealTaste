package com.albert.realmoneyrealtaste.domain.member.exceptions

import com.albert.realmoneyrealtaste.domain.common.ValueObjectValidationException

/**
 * 권한 검증 예외
 */
sealed class RoleValidationException(message: String) : ValueObjectValidationException(message) {
    class MinimumRoleRequired : RoleValidationException("최소 하나의 역할은 유지되어야 합니다")
    class EmptyRoles : RoleValidationException("최소 하나의 역할이 필요합니다")
}
