package com.example.daka.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 数据访问对象（DAO）：定义对打卡记录的增删改查
 */
@Dao
interface DailyRecordDao {

    /** 查询某一天的记录（可能为空） */
    @Query("SELECT * FROM daily_record WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyRecord?

    /** 查询某个日期范围内的所有记录（用于日历显示） */
    @Query("SELECT * FROM daily_record WHERE date >= :start AND date <= :end ORDER BY date")
    fun getBetween(start: String, end: String): Flow<List<DailyRecord>>

    /** 查询全部记录 */
    @Query("SELECT * FROM daily_record ORDER BY date DESC")
    fun getAll(): Flow<List<DailyRecord>>

    /** 保存记录：存在则覆盖（REPLACE） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: DailyRecord)
}
