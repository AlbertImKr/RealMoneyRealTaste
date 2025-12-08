package com.albert.realmoneyrealtaste.application.common.provided

import com.albert.realmoneyrealtaste.domain.common.AggregateRoot
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * 도메인 이벤트 발행 서비스 구현체
 */
@Component
class DomainEventPublisherService(
    private val eventPublisher: ApplicationEventPublisher,
) : DomainEventPublisher {

    override fun publishFrom(aggregate: AggregateRoot) {
        val events = aggregate.drainDomainEvents()
        events.forEach { eventPublisher.publishEvent(it) }
    }
}
