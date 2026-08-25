package com.f1watch.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1watch.data.local.PreferencesManager
import com.f1watch.data.model.Race
import com.f1watch.domain.CountdownCalculator
import com.f1watch.ui.theme.F1CardBg
import com.f1watch.ui.theme.F1CardBorder
import com.f1watch.ui.theme.F1CardSurface
import com.f1watch.ui.theme.F1Dark
import com.f1watch.ui.theme.F1GreenLive
import com.f1watch.ui.theme.F1Red
import com.f1watch.ui.theme.F1TextMuted
import com.f1watch.ui.theme.F1TextSecondary
import com.f1watch.ui.theme.F1TextWhite
import com.f1watch.ui.theme.TeamColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    nextRace: Race?,
    onNavigateBack: () -> Unit,
    onClearCacheAndResync: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentTheme by preferencesManager.widgetThemeFlow.collectAsState(initial = "ferrari")
    var selectedPreviewTab by remember { mutableIntStateOf(0) } // 0: 2x2, 1: 2x4

    val teamThemes = listOf(
        Pair("f1_red", "F1 官方红"),
        Pair("ferrari", "法拉利"),
        Pair("mclaren", "迈凯伦"),
        Pair("redbull", "红牛"),
        Pair("mercedes", "梅赛德斯"),
        Pair("aston_martin", "阿斯顿·马丁"),
        Pair("williams", "威廉姆斯"),
        Pair("sauber", "索伯"),
        Pair("alpine", "阿尔派"),
        Pair("rb", "小牛 RB"),
        Pair("haas", "哈斯"),
        Pair("cadillac", "凯迪拉克")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(F1Dark)
    ) {
        // TopBar with Back Button
        TopAppBar(
            title = {
                Text(
                    text = "设置与个性化",
                    fontWeight = FontWeight.Bold,
                    color = F1TextWhite,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = F1TextWhite
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = F1Dark,
                titleContentColor = F1TextWhite
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Section 1: 桌面小组件定制
            SectionHeader(icon = Icons.Default.Widgets, title = "桌面小组件个性化")

            // Preview Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = F1CardBg),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    TabRow(
                        selectedTabIndex = selectedPreviewTab,
                        containerColor = F1CardBg,
                        contentColor = F1TextWhite,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedPreviewTab]),
                                color = F1Red,
                                height = 2.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedPreviewTab == 0,
                            onClick = { selectedPreviewTab = 0 },
                            text = { Text("2×2 紧凑卡片预览", fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedPreviewTab == 1,
                            onClick = { selectedPreviewTab = 1 },
                            text = { Text("2×4 周末赛程预览", fontSize = 12.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val selectedColor = TeamColors.getColor(currentTheme)
                    val activeRace = nextRace ?: Race(
                        round = "13",
                        raceName = "Italian Grand Prix",
                        circuit = com.f1watch.data.model.Circuit(circuitName = "Autodromo Nazionale Monza", flagEmoji = "🇮🇹"),
                        date = "2026-09-06",
                        time = "13:00:00Z"
                    )
                    val countdown = CountdownCalculator.calculate(activeRace)

                    if (selectedPreviewTab == 0) {
                        // 2x2 Mini Square Preview (1:1 Ratio, Centered)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(156.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF15151E))
                                    .border(1.5.dp, selectedColor, RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    // Header: Flag + Round + Country
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = activeRace.circuit.flagEmoji,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "R${activeRace.round} · ${com.f1watch.util.F1Localization.getCountry(activeRace.circuit.country)}",
                                            color = selectedColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }

                                    // Race Name
                                    Text(
                                        text = com.f1watch.util.F1Localization.getRaceName(activeRace.raceName),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )

                                    // Countdown Focus
                                    Column {
                                        Text(
                                            text = if (countdown.isLive) "LIVE 正在进行" else "正赛倒计时",
                                            color = Color(0xFFA0A0B2),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(1.dp))
                                        Text(
                                            text = countdown.formattedCompact,
                                            fontWeight = FontWeight.Bold,
                                            color = if (countdown.isLive) Color(0xFF00D26A) else Color.White,
                                            fontSize = 15.sp
                                        )
                                    }

                                    // Next Session Footer
                                    Text(
                                        text = if (countdown.isLive) countdown.targetSessionName else "下一阶段: ${countdown.targetSessionName}",
                                        color = selectedColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    } else {
                        // 2x4 Full Weekend Preview with Track Map
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(155.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(F1Dark)
                                .border(1.5.dp, selectedColor, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${activeRace.circuit.flagEmoji} R${activeRace.round} · ${com.f1watch.util.F1Localization.getRaceName(activeRace.raceName)}",
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(selectedColor)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = countdown.formattedCompact, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    // Left: Track map outline
                                    Box(
                                        modifier = Modifier
                                            .width(96.dp)
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF0D0D12))
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        coil.compose.AsyncImage(
                                            model = com.f1watch.ui.util.F1MediaHelper.getCircuitMapUrl(activeRace.circuit.circuitId),
                                            contentDescription = "Track Map",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Right: Sessions list
                                    Column(
                                        modifier = Modifier.weight(1f).fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(F1CardSurface).padding(horizontal = 6.dp, vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("一练 FP1", color = F1TextSecondary, fontSize = 10.sp)
                                            Text("周五 19:30", color = Color.White, fontSize = 10.sp)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(F1CardSurface).padding(horizontal = 6.dp, vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("排位 QUAL", color = F1TextSecondary, fontSize = 10.sp)
                                            Text("周六 22:00", color = Color.White, fontSize = 10.sp)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(selectedColor.copy(alpha = 0.25f)).padding(horizontal = 6.dp, vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("正赛 RACE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                            Text("周日 21:00", color = selectedColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Team Theme Color Selector
            Text(
                text = "选择主队主题配色",
                color = F1TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                teamThemes.forEach { (key, name) ->
                    val color = TeamColors.getColor(key)
                    val isSelected = currentTheme == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) color.copy(alpha = 0.25f) else F1CardBg)
                            .border(1.dp, if (isSelected) color else F1CardBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                scope.launch {
                                    preferencesManager.setWidgetTheme(key)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = name,
                                color = if (isSelected) Color.White else F1TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: 系统日历同步设置
            SectionHeader(icon = Icons.Default.CalendarMonth, title = "系统日历同步设置")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = F1CardBg),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "默认赛前提醒",
                        color = F1TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "一键添加到系统日历时，自动在比赛开始前 15 分钟触发系统闹钟/通知提醒。",
                        color = F1TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: 数据源与网络缓存
            SectionHeader(icon = Icons.Default.Cached, title = "数据信源与离线缓存")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = F1CardBg),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "在线信源：Jolpica F1 Open API",
                                color = F1TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "端点：api.jolpi.ca/ergast/f1/current.json",
                                color = F1TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(F1GreenLive.copy(alpha = 0.2f))
                                .border(1.dp, F1GreenLive, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "🟢 畅通", color = F1GreenLive, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "后台数据自动同步频率",
                        color = F1TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "由于赛历与成绩变动较少，建议设置为 2 天一次以极大节省电量与流量。",
                        color = F1TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    val currentInterval by preferencesManager.syncIntervalHoursFlow.collectAsState(initial = 48)
                    val intervals = listOf(
                        Pair(24, "每天 1 次"),
                        Pair(48, "每 2 天 (推荐)"),
                        Pair(72, "每 3 天")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        intervals.forEach { (hours, label) ->
                            val isSelected = currentInterval == hours
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) F1Red.copy(alpha = 0.2f) else F1CardSurface)
                                    .border(1.dp, if (isSelected) F1Red else F1CardBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        scope.launch {
                                            preferencesManager.setSyncIntervalHours(hours)
                                            com.f1watch.worker.F1SyncWorker.schedulePeriodicSync(context, hours.toLong())
                                            Toast.makeText(context, "已设置后台同步周期为 $label", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else F1TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(F1Red.copy(alpha = 0.15f))
                            .border(1.dp, F1Red.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                onClearCacheAndResync()
                                Toast.makeText(context, "已清除缓存并开始全量同步最新数据！", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Cached, contentDescription = null, tint = F1Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "一键清除缓存并重新同步", color = F1Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 4: 关于应用
            SectionHeader(icon = Icons.Default.Info, title = "关于 F1 Watch")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = F1CardBg),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "F1 Watch (极速赛历) v1.0.0", fontWeight = FontWeight.Black, color = F1TextWhite, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "基于 Kotlin + Jetpack Compose + Jetpack Glance 原生现代化技术栈构建，深度支持 Android 13+ 桌面小部件生态。\n\n本应用为开源非营利项目，所有商标与赛事数据归 Formula 1 官方与数据信源所有。",
                        color = F1TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = F1Red,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = F1TextWhite,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )
    }
}
