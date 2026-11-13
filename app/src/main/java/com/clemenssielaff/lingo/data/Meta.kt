package com.clemenssielaff.lingo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meta")
data class Meta(
    @PrimaryKey val id: String,
    val value: String,
)
