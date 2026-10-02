package com.example.daka.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 打卡记录实体：每天一条记录
 * date 作为主键（格式 yyyy-MM-dd），保证每天唯一
 */
@Entity(tableName = "daily_record")
data class DailyRecord(
    @PrimaryKey val date: String,   // 日期，格式 yyyy-MM-dd
    val slot1: String = "",          // 第 1 次状态
    val slot2: String = "",          // 第 2 次状态
    val slot3: String = "",          // 第 3 次状态
    val slot4: String = ""           // 第 4 次状态
) {
    companion object {
        /** 状态常量 */
        const val DONE = "DONE"     // 打卡
        const val LEAVE = "LEAVE"   // 请假
        const val REST = "REST"     // 休息

        /** 把状态码转成中文显示 */
        fun statusToText(status: String): String = when (status) {
            DONE -> "打卡"
            LEAVE -> "请假"
            REST -> "休息"
            else -> "未打卡"
        }
    }

    /** 取得第 index 个时段（1~4）的状态，越界返回空 */
    fun getSlot(index: Int): String = when (index) {
        1 -> slot1
        2 -> slot2
        3 -> slot3
        4 -> slot4
        else -> ""
    }

    /** 设置第 index 个时段（1~4）的状态 */
    fun setSlot(index: Int, value: String): DailyRecord = when (index) {
        1 -> copy(slot1 = value)
        2 -> copy(slot2 = value)
        3 -> copy(slot3 = value)
        4 -> copy(slot4 = value)
        else -> this
    }

    /** 已完成的次数（只要填了内容就算完成一次） */
    fun filledCount(): Int =
        listOf(slot1, slot2, slot3, slot4).count { it.isNotEmpty() }

    /** 是否四次全部完成 */
    fun isComplete(): Boolean = filledCount() == 4
}
