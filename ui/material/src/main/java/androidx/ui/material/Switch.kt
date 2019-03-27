/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.ui.material

import androidx.ui.baseui.selection.Toggleable
import androidx.ui.baseui.selection.ToggleableState
import androidx.ui.core.DensityReceiver
import androidx.ui.core.MeasureBox
import androidx.ui.core.PxSize
import androidx.ui.core.adapter.Draw
import androidx.ui.core.dp
import androidx.ui.core.max
import androidx.ui.core.min
import androidx.ui.engine.geometry.Offset
import androidx.ui.painting.Canvas
import androidx.ui.painting.Color
import androidx.ui.painting.Paint
import androidx.ui.painting.StrokeCap
import com.google.r4a.Component
import com.google.r4a.Composable
import com.google.r4a.ambient
import com.google.r4a.composer
import com.google.r4a.unaryPlus

// TODO(malkov): maybe intro this wrappers to separate module if heavy-used. Delete later
class SwitchWrapper : Component() {
    var checked: Boolean = false
    var color: Color? = null
    override fun compose() {
        <Switch checked color />
    }
}

@Composable
fun Switch(checked: Boolean, color: Color? = null) {
    val value = if (checked) ToggleableState.Checked else ToggleableState.Unchecked
    <Toggleable value>
        <MeasureBox> constraints ->
            collect {
                val colors = +ambient(Colors)
                <DrawSwitch value color=(color ?: colors.primary) />
            }
            val height = max(
                constraints.minHeight,
                min(constraints.maxHeight, minHeight.toIntPx())
            )
            val width = max(constraints.minWidth, min(constraints.maxWidth, minWidth.toIntPx()))
            layout(width, height) { /* no children to place */ }
        </MeasureBox>
    </Toggleable>
}

@Composable
internal fun DrawSwitch(value: ToggleableState, color: Color) {
    <Draw> canvas, parentSize ->
        drawTrack(canvas, parentSize, value, color)
        drawThumb(canvas, parentSize, value, color)
    </Draw>
}

internal fun DensityReceiver.drawTrack(
    canvas: Canvas,
    parentSize: PxSize,
    state: ToggleableState,
    colors: Color
) {
    val paint = Paint()

    paint.isAntiAlias = true
    paint.color = (if (state == ToggleableState.Checked) colors else uncheckedTrackColor)
        .withAlpha(trackAlpha)
    paint.strokeCap = StrokeCap.round
    paint.strokeWidth = trackHeight.toPx().value

    // TODO(malkov): currently Switch gravity is always CENTER but we need to be flexible
    val centerHeight = parentSize.height.value / 2
    val centerWidth = parentSize.width.value / 2

    val startW = centerWidth - trackWidth.toPx().value / 2
    val endW = centerWidth + trackWidth.toPx().value / 2

    canvas.drawLine(Offset(startW, centerHeight), Offset(endW, centerHeight), paint)
}

// TODO(malkov): figure our animation of this value
internal fun pointPosition(state: ToggleableState) =
    if (state == ToggleableState.Checked) 1f else 0f

internal fun DensityReceiver.drawThumb(
    canvas: Canvas,
    parentSize: PxSize,
    state: ToggleableState,
    colors: Color
) {
    val paint = Paint()

    paint.isAntiAlias = true
    paint.color = if (state == ToggleableState.Checked) colors else uncheckedThumbColor

    // currently I assume that layout gravity of Switch is always CENTER
    val centerHeight = parentSize.height.value / 2
    val centerWidth = parentSize.width.value / 2

    val thumbTrackWidth = trackWidth + pointRadius
    val thumbStartPoint = centerWidth - thumbTrackWidth.toPx().value / 2

    val start = thumbStartPoint + thumbTrackWidth.toPx().value * pointPosition(state)
    canvas.drawCircle(Offset(start, centerHeight), pointRadius.toPx().value, paint)
}

// TODO(malkov): see how it goes and maybe move it to styles or cross-widget defaults
internal val uncheckedThumbColor = Color(0xFFEBEBEB.toInt())
internal val uncheckedTrackColor = Color(0xFF7D7D7D.toInt())
internal val trackAlpha = 75

// TODO(malkov): random numbers which produce the same switch as android.widget.Switch
internal val minWidth = 47.5.dp
internal val minHeight = 27.25.dp

internal val trackHeight = 14.dp
internal val trackWidth = 11.dp
internal val pointRadius = 10.dp