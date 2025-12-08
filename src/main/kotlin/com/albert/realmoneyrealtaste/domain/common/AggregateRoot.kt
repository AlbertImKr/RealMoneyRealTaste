package com.albert.realmoneyrealtaste.domain.common

/**
 * 도메인 애그리거트의 공통 인터페이스
 * 모든 애그리거트는 도메인 이벤트를 가져올 수 있어야 합니다.
 */
fun interface AggregateRoot {
    /**
     * 애그리거트에 축적된 도메인 이벤트를 모두 가져오고 초기화합니다.
     *
     * @return 도메인 이벤트 리스트
     */
    fun drainDomainEvents(): List<Any>
}
