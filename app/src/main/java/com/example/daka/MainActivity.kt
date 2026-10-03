package com.example.daka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.daka.data.DailyRecord
import com.example.daka.ui.DakaViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: DakaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F5)
                ) {
                    DakaScreen(viewModel)
                }
            }
        }
    }
}

/** 顶部绿色 */
private val GreenPrimary = Color(0xFF4CAF50)
private val GreenLight = Color(0xFFE8F5E9)
private val OrangeColor = Color(0xFFFF9800)
private val BlueColor = Color(0xFF2196F3)
private val GrayColor = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DakaScreen(viewModel: DakaViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val record by viewModel.record.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()

    // 弹窗状态：为 null 表示不显示，否则表示要设置哪个时段（1~4）
    var dialogSlot by remember { mutableStateOf<Int?>(null) }

    // 实时时间
    var nowText by remember { mutableStateOf(currentTimeText()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowText = currentTimeText()
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ===== 顶部时间卡片 =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenPrimary)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "每日打卡",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = nowText,
                color = Color.White,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // ===== 今日打卡区 =====
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "打卡进度：${record.filledCount()} / 4",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                // 四个打卡格子
                for (i in 1..4) {
                    SlotRow(
                        index = i,
                        status = record.getSlot(i),
                        onClick = { dialogSlot = i }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ===== 历史记录日历 =====
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "历史记录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                CalendarView(
                    selectedDate = selectedDate,
                    records = allRecords,
                    onDateClick = { date -> viewModel.selectDate(date) }
                )
            }
        }
    }

    // ===== 打卡选项弹窗 =====
    if (dialogSlot != null) {
        val slot = dialogSlot!!
        AlertDialog(
            onDismissRequest = { dialogSlot = null },
            title = { Text("请选择第 $slot 次状态") },
            text = {
                Column {
                    OptionButton("打卡", GreenPrimary) {
                        viewModel.setSlotStatus(slot, DailyRecord.DONE)
                        dialogSlot = null
                    }
                    Spacer(Modifier.height(8.dp))
                    OptionButton("请假", OrangeColor) {
                        viewModel.setSlotStatus(slot, DailyRecord.LEAVE)
                        dialogSlot = null
                    }
                    Spacer(Modifier.height(8.dp))
                    OptionButton("休息", BlueColor) {
                        viewModel.setSlotStatus(slot, DailyRecord.REST)
                        dialogSlot = null
                    }
                    Spacer(Modifier.height(8.dp))
                    OptionButton("清除", GrayColor) {
                        viewModel.setSlotStatus(slot, "")
                        dialogSlot = null
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogSlot = null }) {
                    Text("取消")
                }
            }
        )
    }
}

/** 单个打卡格子 */
@Composable
fun SlotRow(index: Int, status: String, onClick: () -> Unit) {
    val (bg, fg) = when (status) {
        DailyRecord.DONE -> GreenLight to GreenPrimary
        DailyRecord.LEAVE -> Color(0xFFFFF3E0) to OrangeColor
        DailyRecord.REST -> Color(0xFFE3F2FD) to BlueColor
        else -> Color(0xFFF5F5F5) to GrayColor
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(12.dp))
            .border(1.dp, fg.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(fg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$index",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = "第 $index 次",
            fontSize = 16.sp,
            color = Color(0xFF333333)
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = DailyRecord.statusToText(status),
            fontSize = 16.sp,
            color = fg,
            fontWeight = FontWeight.Bold
        )
    }
}

/** 弹窗中的选项按钮 */
@Composable
fun OptionButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(text, fontSize = 16.sp)
    }
}

/**
 * 简单的月历视图
 * @param selectedDate 当前选中的日期
 * @param records 日期 -> 记录
 * @param onDateClick 点击某天的回调
 */
@Composable
fun CalendarView(
    selectedDate: String,
    records: Map<String, DailyRecord>,
    onDateClick: (String) -> Unit
) {
    // 当前显示的月份（默认本月）
    var yearMonth by remember { mutableStateOf(nowYearMonth()) }

    val (year, month) = yearMonth
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // 计算当月天数与 1 号是星期几
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=周日
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val leadingBlanks = firstDayOfWeek - 1 // 前面空几格（周一开头可自行调整）

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))) {
        Column(modifier = Modifier.padding(8.dp)) {
            // 月份切换
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    yearMonth = if (month == 1) year - 1 to 12 else year to month - 1
                }) { Text("◀") }
                Spacer(Modifier.weight(1f))
                Text("${year}年${month}月", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    yearMonth = if (month == 12) year + 1 to 1 else year to month + 1
                }) { Text("▶") }
            }

            // 星期表头
            Row(modifier = Modifier.fillMaxWidth()) {
                for (w in listOf("日", "一", "二", "三", "四", "五", "六")) {
                    Text(
                        text = w,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = GrayColor
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            // 日期格子
            val totalCells = leadingBlanks + daysInMonth
            val rows = (totalCells + 6) / 7
            var dayCounter = 1
            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        if (cellIndex < leadingBlanks || dayCounter > daysInMonth) {
                            Spacer(Modifier.weight(1f).height(44.dp))
                        } else {
                            val day = dayCounter
                            val dateStr = String.format(
                                Locale.getDefault(), "%04d-%02d-%02d", year, month, day
                            )
                            val rec = records[dateStr]
                            DayCell(
                                day = day,
                                record = rec,
                                selected = dateStr == selectedDate,
                                isToday = dateStr == todayStr(sdf),
                                onClick = { onDateClick(dateStr) }
                            )
                            dayCounter++
                        }
                    }
                }
            }
        }
    }
}

/** 日历里的单个日期格子 */
@Composable
fun DayCell(
    day: Int,
    record: DailyRecord?,
    selected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    // 根据记录决定底部小标记
    val marker = when {
        record == null -> ""
        record.isComplete() -> "✓"
        record.slot1.isNotEmpty() || record.slot2.isNotEmpty() ||
                record.slot3.isNotEmpty() || record.slot4.isNotEmpty() -> "${record.filledCount()}"
        else -> ""
    }
    val markerColor = when {
        record == null -> GrayColor
        record.isComplete() -> GreenPrimary
        else -> OrangeColor
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when {
                        selected -> GreenPrimary
                        isToday -> GreenLight
                        else -> Color.Transparent
                    },
                    RoundedCornerShape(8.dp)
                )
                .border(
                    1.dp,
                    if (selected) GreenPrimary else Color(0xFFEEEEEE),
                    RoundedCornerShape(8.dp)
                )
                .clickable { onClick() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$day",
                fontSize = 14.sp,
                color = if (selected) Color.White else Color(0xFF333333)
            )
            if (marker.isNotEmpty()) {
                Text(
                    text = marker,
                    fontSize = 10.sp,
                    color = if (selected) Color.White else markerColor
                )
            }
        }
    }
}

// ===== 工具函数 =====

private fun currentTimeText(): String =
    SimpleDateFormat("yyyy年MM月dd日 EEEE HH:mm:ss", Locale.CHINA).format(Date())

private fun nowYearMonth(): Pair<Int, Int> {
    val cal = Calendar.getInstance()
    return cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
}

private fun todayStr(sdf: SimpleDateFormat): String = sdf.format(Date())
