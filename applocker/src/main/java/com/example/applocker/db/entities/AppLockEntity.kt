package com.example.applocker.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "app_lock_table")
data class AppLockEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int?=null,
    @ColumnInfo(name = "packageName")
    val packageName: String
)