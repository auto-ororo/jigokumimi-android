package com.ororo.auto.jigokumimi.repository

import kotlinx.coroutines.flow.Flow

interface IDeviceRepository {

    /**
     * 端末の通信状態を監視する
     */
    fun observeNetworkConnection() : Flow<Boolean>
}