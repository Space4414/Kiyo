package com.space4414.kiyo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.space4414.kiyo.ui.component.AmbientBackdrop
import com.space4414.kiyo.ui.component.FrostedCard
import com.space4414.kiyo.ui.theme.KiyoCharcoalLight
import com.space4414.kiyo.ui.theme.KiyoOnSurfaceMuted
import com.space4414.kiyo.ui.theme.KiyoOutline
import com.space4414.kiyo.ui.theme.KiyoTeal
import com.space4414.kiyo.ui.viewmodel.PlayerViewModel

private val EQ_FREQ_LABELS = listOf(
    "20", "25", "31", "40", "50", "63", "80", "100",
    "125", "160", "200", "250", "315", "400", "500", "630",
    "800", "1k", "1.2k", "1.6k", "2k", "2.5k", "3.1k", "4k",
    "5k", "6.3k", "8k", "10k", "12k", "16k", "20k"
)

private const val DB_MAX = 12f
private const val DB_MIN = -12f
private const val FADER_HEIGHT_DP = 200

@Composable
fun DspScreen(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val eqBands by viewModel.eqBands.collectAsState()
    val preAmp by viewModel.preAmpGain.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackdrop(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Audio DSP",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
            )
            Text(
                text = "31-Band Equalizer",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 16.dp),
            )

            FrostedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((FADER_HEIGHT_DP + 80).dp),
                cornerRadius = 20.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    PreAmpColumn(
                        gain = preAmp,
                        onGainChange = viewModel::setPreAmpGain,
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(KiyoOutline),
                    )

                    val hScroll = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(hScroll),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            eqBands.forEachIndexed { idx, gain ->
                                EqFaderColumn(
                                    frequency = EQ_FREQ_LABELS.getOrElse(idx) { "$idx" },
                                    gainDb = gain,
                                    onGainChange = { newGain -> viewModel.setEqBand(idx, newGain) },
                                )
                            }
                        }

                        EqCurveOverlay(
                            bands = eqBands,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            DbLegend()
        }
    }
}

@Composable
private fun PreAmpColumn(gain: Float, onGainChange: (Float) -> Unit) {
    Column(
        modifier = Modifier
            .width(52.dp)
            .fillMaxHeight()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "Pre",
            style = MaterialTheme.typography.labelMedium,
            color = KiyoTeal,
            fontSize = 10.sp,
        )
        Text(
            "${if (gain >= 0f) "+" else ""}${gain.toInt()}dB",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 9.sp,
        )
        VerticalFader(
            gainDb = gain,
            onGainChange = onGainChange,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            thumbColor = KiyoTeal,
        )
    }
}

@Composable
private fun EqFaderColumn(
    frequency: String,
    gainDb: Float,
    onGainChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(28.dp)
            .height(FADER_HEIGHT_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "${if (gainDb >= 0f) "+" else ""}${gainDb.toInt()}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 8.sp,
        )
        VerticalFader(
            gainDb = gainDb,
            onGainChange = onGainChange,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            thumbColor = Color.White,
        )
        Text(
            text = frequency,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 8.sp,
        )
    }
}

@Composable
private fun VerticalFader(
    gainDb: Float,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    thumbColor: Color = Color.White,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        var trackHeightPx = remember { 0f }

        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(KiyoOutline),
        )

        val normalizedGain = (gainDb - DB_MIN) / (DB_MAX - DB_MIN)
        val filledFraction = (normalizedGain - 0.5f).let {
            if (gainDb >= 0f) it else it
        }

        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .drawWithCache {
                    onDrawBehind {
                        val centerY = size.height / 2f
                        val thumbY = size.height * (1f - normalizedGain)
                        val top = minOf(centerY, thumbY)
                        val bottom = maxOf(centerY, thumbY)
                        drawRect(
                            color = thumbColor.copy(alpha = 0.5f),
                            topLeft = Offset(0f, top),
                            size = androidx.compose.ui.geometry.Size(size.width, bottom - top),
                        )
                    }
                },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(gainDb) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        val rangePx = size.height.toFloat()
                        val deltaDb = (dragAmount / rangePx) * (DB_MAX - DB_MIN) * -1f
                        val newGain = (gainDb + deltaDb).coerceIn(DB_MIN, DB_MAX)
                        onGainChange(newGain)
                    }
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(top = ((1f - normalizedGain) * FADER_HEIGHT_DP * 0.8f).dp)
                .height(14.dp)
                .padding(horizontal = 2.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(thumbColor, thumbColor.copy(alpha = 0.3f)),
                    )
                ),
        )
    }
}

@Composable
private fun EqCurveOverlay(bands: FloatArray, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.drawWithCache {
            onDrawWithContent {
                drawContent()
                if (bands.isEmpty()) return@onDrawWithContent

                val path = Path()
                val w = size.width
                val h = size.height
                val topPadPx = 20f
                val botPadPx = 30f
                val usableH = h - topPadPx - botPadPx

                bands.forEachIndexed { idx, gain ->
                    val x = (idx.toFloat() / (bands.size - 1).coerceAtLeast(1)) * w
                    val normalized = (gain - DB_MIN) / (DB_MAX - DB_MIN)
                    val y = topPadPx + usableH * (1f - normalized)
                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            KiyoTeal.copy(alpha = 0.9f),
                            Color(0xFF8B5CF6).copy(alpha = 0.9f),
                            KiyoTeal.copy(alpha = 0.9f),
                        ),
                    ),
                    style = Stroke(
                        width = 3f,
                        cap = StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round,
                        miter = 10f,
                    ),
                )

                val glowPath = Path().apply { addPath(path) }
                drawPath(
                    path = glowPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            KiyoTeal.copy(alpha = 0.25f),
                            Color(0xFF8B5CF6).copy(alpha = 0.25f),
                            KiyoTeal.copy(alpha = 0.25f),
                        ),
                    ),
                    style = Stroke(
                        width = 10f,
                        cap = StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round,
                        miter = 10f,
                    ),
                )
            }
        },
    )
}

@Composable
private fun DbLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        listOf("+12dB", "+6dB", "0dB", "-6dB", "-12dB").forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = KiyoOnSurfaceMuted,
                fontSize = 10.sp,
            )
        }
    }
}
