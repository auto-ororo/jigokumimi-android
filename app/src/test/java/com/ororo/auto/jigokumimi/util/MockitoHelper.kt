package com.ororo.auto.jigokumimi.util

import org.mockito.Mockito

class MockitoHelper {
    companion object {
        fun <T> any(): T {
            return Mockito.any()
                ?: null as T
        }

        fun <T> eq(value: T): T {
            return if (value != null)
                Mockito.eq(value)
            else
                null
                    ?: null as T
        }
    }

}