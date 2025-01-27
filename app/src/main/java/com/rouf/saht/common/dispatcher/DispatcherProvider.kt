package com.rouf.saht.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DispatcherProvider @Inject constructor() {
    companion object {
        val io: CoroutineDispatcher = Dispatchers.IO
        val main: CoroutineDispatcher = Dispatchers.Main
        val default: CoroutineDispatcher = Dispatchers.Default
    }
}