package com.example.mobileapp.utils

/**
 * A generic sealed class that wraps the result of any operation exposed to the
 * presentation layer. Standardizes Loading / Success / Error states so every
 * ViewModel and Activity speak the same language.
 */
sealed class Resource<out T> {
    /** Operation is in progress. */
    object Loading : Resource<Nothing>()

    /** Operation completed successfully with [data]. */
    data class Success<T>(val data: T) : Resource<T>()

    /** Operation failed with an error [message]. */
    data class Error(val message: String) : Resource<Nothing>()
}
