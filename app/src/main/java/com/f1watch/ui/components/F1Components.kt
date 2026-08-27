package com.f1watch.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1watch.data.model.Race
import com.f1watch.data.model.SessionItem
import com.f1watch.data.model.SessionStatus
import com.f1watch.domain.CountdownResult
import com.f1watch.ui.theme.F1CardBg
import com.f1watch.ui.theme.F1CardBorder
import com.f1watch.ui.theme.F1CardSurface
import com.f1watch.ui.theme.F1Dark
import com.f1watch.ui.theme.F1GreenLive
import com.f1watch.ui.theme.F1Red
import com.f1watch.ui.theme.F1TextMuted
import com.f1watch.ui.theme.F1TextSecondary
import com.f1watch.ui.theme.F1TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun F1TopBar(
    title: String,
    onOpenCustomizer: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = F1TextWhite,
                letterSpacing = (-0.5).sp
            )
        },
        actions = {
            IconButton(onClick = onOpenCustomizer) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "小组件定制",
                    tint = F1TextSecondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = F1TextWhite
        )
    )
}

@Composable
fun SessionStatusBadge(status: SessionStatus) {
    val (bgColor, textColor, text) = when (status) {
        SessionStatus.LIVE -> Triple(
            F1GreenLive.copy(alpha = 0.2f),
            F1GreenLive,
            "🟢 LIVE 进行中"
        )
        SessionStatus.COMPLETED -> Triple(
            F1TextMuted.copy(alpha = 0.2f),
            F1TextMuted,
            "已结束"
        )
        SessionStatus.UPCOMING -> Triple(
            F1Red.copy(alpha = 0.2f),
            F1Red,
            "待开始"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HeroNextRaceCard(
    race: Race,
    countdown: CountdownResult,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
    onSyncCalendar: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = F1CardBg),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Round + Flag + Country
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = race.circuit.flagEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ROUND ${race.round}",
                        color = F1Red,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
                SessionStatusBadge(
                    if (countdown.isLive) SessionStatus.LIVE else SessionStatus.UPCOMING
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = com.f1watch.util.F1Localization.getRaceName(race.raceName),
                fontWeight = FontWeight.Black,
                color = F1TextWhite,
                fontSize = 20.sp
            )

            Text(
                text = "📍 ${com.f1watch.util.F1Localization.getCircuitName(race.circuit.circuitName)} · ${com.f1watch.util.F1Localization.getLocality(race.circuit.locality)}",
                color = F1TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Countdown box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(F1CardSurface)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = if (countdown.isLive) "当前比赛状态" else "正赛倒计时",
                        color = F1TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = countdown.formattedCompact,
                        color = if (countdown.isLive) F1GreenLive else F1TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = countdown.formattedLong,
                        color = F1TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(F1CardSurface)
                        .clickable { onSyncCalendar() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = F1Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "一键加日历",
                        color = F1TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isExpanded) F1Red.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { onToggleExpand() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isExpanded) "收起赛程" else "查看全周末赛程",
                        color = F1Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = F1Red,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expandable Sessions List directly inside Hero Card!
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    // High-Impact Track Outline Card with graceful fallback
                    CircuitMapCard(
                        circuitId = race.circuit.circuitId,
                        circuitName = race.circuit.circuitName,
                        locality = race.circuit.locality
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "本站完整周末时间表 (已转为本地时间)",
                        color = F1TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    race.getAllSessions().forEach { session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(F1CardSurface.copy(alpha = 0.7f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (session.status == SessionStatus.LIVE) F1GreenLive else F1Red)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = com.f1watch.util.F1Localization.getSessionName(session.name),
                                    color = F1TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = session.sessionTime.formatLocalDayTime(),
                                    color = F1TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                SessionStatusBadge(status = session.status)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

/**
 * 车手头像优雅降级组件：加载失败或无网络时显示车手3字母代码与车队色彩底晕，替代单一空白圆
 */
@Composable
fun DriverAvatar(
    driverCode: String,
    driverFullName: String,
    teamColor: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 32.dp,
    borderWidth: androidx.compose.ui.unit.Dp = 0.dp,
    borderColor: Color = Color.Transparent
) {
    val headshotUrl by com.f1watch.ui.util.F1MediaHelper.rememberDriverHeadshotUrl(
        driverCode = driverCode,
        driverFullName = driverFullName
    )

    val codeText = driverCode.take(3).uppercase().ifEmpty {
        driverFullName.split(" ").lastOrNull()?.take(3)?.uppercase() ?: "DRV"
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(F1CardSurface)
            .then(if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, CircleShape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (headshotUrl.isNotEmpty()) {
            SubcomposeAsyncImage(
                model = headshotUrl,
                contentDescription = driverFullName,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            ) {
                val state = painter.state
                if (state is AsyncImagePainter.State.Success) {
                    SubcomposeAsyncImageContent()
                } else {
                    DriverAvatarFallback(codeText = codeText, teamColor = teamColor)
                }
            }
        } else {
            DriverAvatarFallback(codeText = codeText, teamColor = teamColor)
        }
    }
}

@Composable
private fun DriverAvatarFallback(
    codeText: String,
    teamColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(teamColor.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = codeText,
            color = F1TextWhite,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = (-0.5).sp
        )
    }
}

/**
 * 赛车涂装优雅降级组件：加载失败或无网络时显示车队半透明 Logo 与队名，替代单一黑色暗盒
 */
@Composable
fun CarLiveryCard(
    constructorId: String,
    constructorName: String,
    teamColor: Color,
    modifier: Modifier = Modifier
) {
    val carUrl = com.f1watch.ui.util.F1MediaHelper.getCarImageUrl(constructorId)
    val badgeUrl = com.f1watch.ui.util.F1MediaHelper.getTeamBadgeUrl(constructorId)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4.0f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0C0C12)),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = carUrl,
            contentDescription = constructorName,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.0f),
            contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Success) {
                SubcomposeAsyncImageContent()
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = badgeUrl,
                        contentDescription = constructorName,
                        modifier = Modifier.size(36.dp),
                        alpha = 0.4f
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = constructorName,
                        color = teamColor.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * 赛道地图优雅降级组件：加载失败或无网络时显示方格旗与赛道名称，替代单调空白区域
 */
@Composable
fun CircuitMapCard(
    circuitId: String,
    circuitName: String,
    locality: String = "",
    modifier: Modifier = Modifier
) {
    val mapUrl = com.f1watch.ui.util.F1MediaHelper.getCircuitMapUrl(circuitId)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2.0f)
            .clip(RoundedCornerShape(12.dp))
            .background(F1Dark)
            .border(1.dp, F1CardBorder, RoundedCornerShape(12.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = mapUrl,
            contentDescription = circuitName,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Success) {
                SubcomposeAsyncImageContent()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🏁",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = com.f1watch.util.F1Localization.getCircuitName(circuitName),
                        color = F1TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Location badge top-left
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(6.dp))
                .background(F1CardSurface.copy(alpha = 0.9f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏁 ${com.f1watch.util.F1Localization.getCircuitName(circuitName)}",
                color = F1TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            if (locality.isNotBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "· ${com.f1watch.util.F1Localization.getLocality(locality)}",
                    color = F1TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * 全局暗夜流光背景：在界面顶部渲染主队主题色流光，由屏幕最顶端向下自然漫反射渐变至纯黑
 */
@Composable
fun AmbientGlowBackground(
    themeColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09090E))
    ) {
        // Atmospheric Seamless Top-Down Ambient Canopy Glow
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        ) {
            val h = size.height

            // Smooth Multi-stop Ambient Falloff (No circular dome lines)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        themeColor.copy(alpha = 0.38f),
                        themeColor.copy(alpha = 0.22f),
                        themeColor.copy(alpha = 0.10f),
                        themeColor.copy(alpha = 0.035f),
                        themeColor.copy(alpha = 0.008f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                )
            )
        }

        content()
    }
}

/**
 * 机械翻牌式（Split-Flap）赛道精密倒计时
 */
@Composable
fun F1TimingCountdownBoard(
    countdown: CountdownResult,
    themeColor: Color,
    modifier: Modifier = Modifier
) {
    if (countdown.isLive) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
                .background(F1GreenLive.copy(alpha = 0.15f))
                .border(1.dp, F1GreenLive, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(F1GreenLive)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "🟢 ${countdown.targetSessionName} 正在进行中 (LIVE)",
                color = F1TextWhite,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SplitFlapDigitBlock(value = String.format("%02d", countdown.days), label = "天", themeColor = themeColor)
            Text(
                text = ":",
                color = F1TextMuted,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 5.dp, end = 5.dp, bottom = 16.dp)
            )
            SplitFlapDigitBlock(value = String.format("%02d", countdown.hours), label = "时", themeColor = themeColor)
            Text(
                text = ":",
                color = F1TextMuted,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 5.dp, end = 5.dp, bottom = 16.dp)
            )
            SplitFlapDigitBlock(value = String.format("%02d", countdown.minutes), label = "分", themeColor = themeColor)
            Text(
                text = ":",
                color = F1TextMuted,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 5.dp, end = 5.dp, bottom = 16.dp)
            )
            SplitFlapDigitBlock(value = String.format("%02d", countdown.seconds), label = "秒", themeColor = themeColor)
        }
    }
}

/**
 * 单个机械翻牌分块（Split-Flap Unit）
 */
@Composable
fun SplitFlapDigitBlock(
    value: String,
    label: String,
    themeColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 机械翻牌外壳 (带细致的物理分片与受光阴影)
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF101018))
                .border(1.dp, Color(0xFF26263A), RoundedCornerShape(6.dp))
        ) {
            // 上半片 (顶光微亮)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF222232),
                                Color(0xFF14141E)
                            )
                        )
                    )
            )
            // 下半片 (内凹微暗)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF101016),
                                Color(0xFF09090E)
                            )
                        )
                    )
            )

            // 数字主体
            Text(
                text = value,
                color = F1TextWhite,
                fontWeight = FontWeight.Black,
                fontSize = 21.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            // 中间机械折线裂缝 (Split Flap Hinge Line)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(1.2.dp)
                    .background(Color(0xFF06060A))
            )

            // 左右两侧微型机械固定卡扣
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(width = 2.dp, height = 5.dp)
                    .background(Color(0xFF42425A))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(width = 2.dp, height = 5.dp)
                    .background(Color(0xFF42425A))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = F1TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
