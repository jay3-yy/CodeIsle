package com.openisle.android.domain.util

/**
 * 一个通用的密封类，用于包装数据请求的结果。
 * @param T 成功时返回的数据类型。
 */
sealed class Result<out T> {
    /**
     * 表示请求成功。
     * @property value 成功返回的数据。
     */
    data class Success<out T>(val value: T) : Result<T>()

    /**
     * 表示请求失败。
     * @property exception 失败时抛出的异常。
     *
     * ▼▼▼ 核心修正：必须继承自 Result<Nothing> ▼▼▼
     * 这使得 Result.Error 可以被用在任何 Result<T> 类型的 when 语句中。
     */
    data class Error(val exception: Exception) : Result<Nothing>()
}