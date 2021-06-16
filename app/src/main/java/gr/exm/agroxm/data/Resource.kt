package gr.exm.agroxm.data

import arrow.core.Either
import arrow.core.flatMap

/**
 * Status of a resource that is provided to the UI.
 *
 *
 * These are usually created by the Repository classes where they return
 * `LiveData<Resource<T>>` to pass back the latest data to the UI with its fetch status.
 */
enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

/**
 * A generic class that holds a value with its loading status.
 */
data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

        fun <T> empty(): Resource<T> {
            return success(null)
        }
    }
}

/*fun <T: Any> Either<Error, T>.resource(): Resource<T> {
    return mapLeft<Resource<T>> {
        Resource.error(it.message ?: "No error message")
    }.map {
        Resource.success(it)
    }
}*/

/*
fun <T: Any> resource(codeBlock: () -> Either<Error, T>): Resource<T> {
    return codeBlock()
        .mapLeft { error -> Resource.error(error.message ?: "No error message") }
        .map { data -> Resource.success(data) }
}*/
