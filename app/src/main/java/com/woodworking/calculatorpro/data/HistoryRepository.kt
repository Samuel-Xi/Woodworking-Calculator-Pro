package com.woodworking.calculatorpro.data

import kotlinx.coroutines.flow.Flow

/**
 * Thin facade over [HistoryDao]. The repository layer is intentionally simple
 * because the app has a single data source (local Room). It still exists so
 * view-models depend on an abstraction rather than a DAO directly.
 */
class HistoryRepository(private val dao: HistoryDao) {

    fun observeAll(): Flow<List<HistoryEntity>> = dao.observeAll()

    fun observeByTool(toolKey: String): Flow<List<HistoryEntity>> =
        dao.observeByTool(toolKey)

    suspend fun save(toolKey: String, title: String, summary: String): Long =
        dao.insert(
            HistoryEntity(
                toolKey = toolKey,
                title = title,
                summary = summary,
                createdAt = System.currentTimeMillis()
            )
        )

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun clear() = dao.clear()
}
