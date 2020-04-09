package com.ororo.auto.jigokumimi.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * アプリケーション全体で使用される共通関数を定義
 *
 */
object Util {

    /**
     * 現在日時をyyyy/MM/dd HH:mm:ss形式で取得する.<br>
     */
    fun getNowDate(): String {
        val df = SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        val date = Date(System.currentTimeMillis());
        return df.format(date);
    }

}
