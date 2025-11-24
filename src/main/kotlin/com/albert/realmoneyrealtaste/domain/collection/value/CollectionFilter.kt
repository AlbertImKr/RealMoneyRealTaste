package com.albert.realmoneyrealtaste.domain.collection.value

enum class CollectionFilter(val value: String) {
    ALL("all"),
    PUBLIC("public");

    companion object {
        fun from(value: String?): CollectionFilter =
            entries.find { it.value == value?.lowercase() } ?: ALL
    }
}
