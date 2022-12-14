package com.danigutiadan.mo2o_test.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.danigutiadan.mo2o_test.data.Result.Status.ERROR
import com.danigutiadan.mo2o_test.data.Result.Status.SUCCESS
import kotlinx.coroutines.Dispatchers

/**
 * The database serves as the single source of truth.
 * Therefore UI can receive data updates from database only.
 * Function notify UI about:
 * [Result.Status.SUCCESS] - with data from database
 * [Result.Status.ERROR] - if error has occurred from any source
 * [Result.Status.LOADING]
 */

fun <T> resultLiveData(networkCall: suspend () -> Result<T>): LiveData<Result<T>> =
    liveData(Dispatchers.IO) {
        emit(Result.loading())
        val responseStatus = networkCall.invoke()
        if (responseStatus.status == SUCCESS && responseStatus.data != null) {
            emit(Result.success(responseStatus.data))
        } else if (responseStatus.status == ERROR) {
            emit(
                Result.error<T>(
                    message = responseStatus.message
                        ?: "", code = responseStatus.code
                )
            )
        }
    }
