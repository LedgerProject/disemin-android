package gr.exm.agroxm.data

import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

fun Request.path(): String = this.url.encodedPath

fun Response.path(): String = this.request.path()
