package com.albert.realmoneyrealtaste.application.common.provided

import com.albert.realmoneyrealtaste.domain.common.AggregateRoot

/**
 * 도메인 이벤트 발행 포트
 */
fun interface DomainEventPublisher {
    /**
     * 애그리거트의 도메인 이벤트를 발행합니다.
     *
     * @param aggregate 도메인 이벤트를 가진 애그리거트
     */
    fun publishFrom(aggregate: AggregateRoot)
}
