package com.ororo.auto.jigokumimi.ui.common

/**
 * リスト内のアイテムをクリックしたときの動作
 *
 */
class ItemClick(val block: (Int) -> Unit) {

    fun onClick(itemIndex: Int) = block(itemIndex)
}

