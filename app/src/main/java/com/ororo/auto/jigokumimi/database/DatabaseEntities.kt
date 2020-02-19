package com.ororo.auto.jigokumimi.database

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * ローカルDBとの通信で使うEntity群を定義
 */


/**
 * DatabaseVideo represents a video entity in the database.
 *
 */
@Entity
data class DatabaseSong constructor(
    @PrimaryKey
    val id: String
)