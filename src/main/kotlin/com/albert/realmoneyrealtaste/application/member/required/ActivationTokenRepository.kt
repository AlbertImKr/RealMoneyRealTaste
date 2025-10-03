package com.albert.realmoneyrealtaste.application.member.required

import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import org.springframework.data.repository.Repository

interface ActivationTokenRepository : Repository<ActivationToken, Long> {

    fun save(token: ActivationToken): ActivationToken

    fun findByToken(token: String): ActivationToken?

    fun delete(token: ActivationToken)

    fun findByMemberId(memberId: Long): ActivationToken?
}
