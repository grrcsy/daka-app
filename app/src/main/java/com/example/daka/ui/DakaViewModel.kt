package com.example.daka.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.daka.data.AppDatabase
import com.example.daka.data.DailyRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 界面逻辑与数据之间的桥梁（ViewModel）
 */
class DakaViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getInstance(app).dailyRecordDao()

    // 当前选择的日期（默认今天），格式 yyyy-MM-dd
    private val _selectedDate = MutableStateFlow(today())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // 当前选中日期的打卡记录
    private val _record = MutableStateFlow(DailyRecord(today()))
    val record: StateFlow<DailyRecord> = _record.asStateFlow()

    // 日历所需：所有记录（日期 -> 记录 的映射）
    private val _allRecords = MutableStateFlow<Map<String, DailyRecord>>(emptyMap())
    val allRecords: StateFlow<Map<String, DailyRecord>> = _allRecords.asStateFlow()

    init {
        // 加载全部记录（用于日历）
        viewModelScope.launch {
            dao.getAll().collect { list ->
                _allRecords.value = list.associateBy { it.date }
            }
        }
        // 加载当前选中日期的记录
        loadRecord(_selectedDate.value)
    }

    /** 切换选中的日期 */
    fun selectDate(date: String) {
        _selectedDate.value = date
        loadRecord(date)
    }

    /** 从数据库加载指定日期的记录（没有则新建空记录） */
    private fun loadRecord(date: String) {
        viewModelScope.launch {
            _record.value = dao.getByDate(date) ?: DailyRecord(date)
        }
    }

    /**
     * 为选中日期的第 slotIndex（1~4）个时段设置状态
     * @param slotIndex 1~4
     * @param status DONE / LEAVE / REST，传空字符串表示清空
     */
    fun setSlotStatus(slotIndex: Int, status: String) {
        val current = _record.value
        val updated = current.setSlot(slotIndex, status)
        _record.value = updated
        viewModelScope.launch {
            dao.upsert(updated)
        }
    }

    /** 今天日期字符串 */
    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}
