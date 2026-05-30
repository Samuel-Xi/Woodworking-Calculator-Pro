package com.woodworking.calculatorpro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One saved calculation. We keep the schema flat and serialise complex inputs
 * to plain strings so the database stays migration-friendly.
 */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val toolKey: String,            // e.g. "miter", "flooring"
    val title: String,              // human-readable summary
    val summary: String,            // multi-line summary, "key: value" per line
    val createdAt: Long,            // epoch millis
)
