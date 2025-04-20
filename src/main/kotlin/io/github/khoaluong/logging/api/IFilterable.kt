package io.github.khoaluong.logging.api

interface IFilterable<T> {
    val filters: List<Filter>

    fun addFilter(filter: Filter)
    fun addFilters(vararg filters: Filter)
    fun addFilters(filters: Iterable<Filter>)
    fun removeFilter(filter: Filter)
    fun filterAll(event: T): Boolean
}