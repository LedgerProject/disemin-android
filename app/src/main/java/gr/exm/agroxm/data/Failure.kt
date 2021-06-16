package gr.exm.agroxm.data

import androidx.annotation.Keep

/**
 * Base Class for handling errors/failures/exceptions.
 */
@Keep
sealed class Failure {
    object NetworkError : Failure()
    object ServerError : Failure()
    class UnknownError(private val message: String) : Failure() {
        override fun toString(): String = message
    }
}