/*
 * Copyright 2019 The Android Open Source Project
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

package androidx.r4a

import android.os.Handler
import androidx.ui.core.Constraints
import androidx.ui.core.ComplexMeasureBox
import androidx.ui.core.Dp
import androidx.ui.core.Draw
import androidx.ui.core.IntPx
import androidx.ui.core.MeasureBox
import androidx.ui.core.Layout
import androidx.ui.core.ComplexLayout
import androidx.ui.core.CraneWrapper
import androidx.ui.core.dp
import androidx.ui.core.ipx
import androidx.ui.core.max
import androidx.ui.core.toRect
import androidx.ui.layout.Align
import androidx.ui.layout.Alignment
import androidx.ui.layout.Center
import androidx.ui.layout.Column
import androidx.ui.layout.ConstrainedBox
import androidx.ui.layout.Container
import androidx.ui.layout.CrossAxisAlignment
import androidx.ui.layout.EdgeInsets
import androidx.ui.layout.FlexColumn
import androidx.ui.layout.FlexRow
import androidx.ui.layout.MainAxisAlignment
import androidx.ui.layout.MainAxisSize
import androidx.ui.layout.Padding
import androidx.ui.layout.Row
import androidx.ui.layout.Stack
import androidx.ui.painting.Color
import androidx.ui.painting.Paint
import com.google.r4a.Children
import com.google.r4a.Composable
import com.google.r4a.Model
import com.google.r4a.composer
import com.google.r4a.effectOf
import com.google.r4a.memo
import com.google.r4a.onCommit
import com.google.r4a.unaryPlus

/**
 * Draws a rectangle of a specified dimension, or to its max incoming constraints if
 * dimensions are not specified.
 */
@Composable
fun SizedRectangle(color: Color, width: Dp? = null, height: Dp? = null) {
    <MeasureBox> constraints ->
        collect {
            <DrawRectangle color />
        }
        val widthPx = width?.toIntPx() ?: constraints.maxWidth
        val heightPx = height?.toIntPx() ?: constraints.maxHeight
        layout(widthPx, heightPx) {}
    </MeasureBox>
}

/**
 * A widget that forces its only child to be as wide as its min intrinsic width.
 */
@Composable
fun IntrinsicWidth(@Children() children: () -> Unit) {
    <ComplexMeasureBox>
        val child = collect(children).first()

        layout { constraints ->
            // Force child be as wide as its min intrinsic width.
            val width = child.minIntrinsicWidth(constraints.minHeight)
            val childConstraints = Constraints(
                width,
                width,
                constraints.minHeight,
                constraints.maxHeight
            )
            val childPlaceable = child.measure(childConstraints)
            layoutResult(childPlaceable.width, childPlaceable.height) {
                childPlaceable.place(IntPx.Zero, IntPx.Zero)
            }
        }

        minIntrinsicWidth { h ->
            child.minIntrinsicWidth(h)
        }
        maxIntrinsicWidth { h ->
            child.minIntrinsicWidth(h)
        }
        minIntrinsicHeight { w ->
            child.minIntrinsicHeight(w)
        }
        maxIntrinsicHeight { w ->
            child.maxIntrinsicHeight(w)
        }
    </ComplexMeasureBox>
}

@Composable
fun Wrapper(@Children() children: () -> Unit) {
    <ComplexMeasureBox>
        val child = collect(children).first()
        layout { constraints ->
            // Check the default intrinsic methods used by MeasureBoxes.
            // TODO(popam): make this a proper test instead
            require(child.minIntrinsicWidth(IntPx.Infinity) == 90.ipx)
            require(child.maxIntrinsicWidth(IntPx.Infinity) == 450.ipx)
            require(child.minIntrinsicHeight(IntPx.Infinity) == 30.ipx)
            require(child.maxIntrinsicHeight(IntPx.Infinity) == 150.ipx)
            val placeable = child.measure(constraints)
            layoutResult(placeable.width, placeable.height) {
                placeable.place(IntPx.Zero, IntPx.Zero)
            }
        }
    </ComplexMeasureBox>
}

/**
 * Draws an rectangle of fixed (80.dp, 80.dp) size, while providing intrinsic dimensions as well.
 */
@Composable
fun RectangleWithIntrinsics(color: Color) {
    <ComplexMeasureBox> collect {
        <DrawRectangle color />
    }
        layout {
            layoutResult(80.ipx, 80.ipx) {}
        }
        minIntrinsicWidth { 30.ipx }
        maxIntrinsicWidth { 150.ipx }
        minIntrinsicHeight { 30.ipx }
        maxIntrinsicHeight { 150.ipx }
    </ComplexMeasureBox>
}

@Composable
fun FlexRowUsage() {
    <FlexRow>
        expanded(flex=2f) {
            <Center>
                <SizedRectangle color=Color(0xFF0000FF.toInt()) width=40.dp height=40.dp />
            </Center>
            <SizedRectangle color=Color(0xFF0000FF.toInt()) height=40.dp />
        }
        inflexible {
            <SizedRectangle color=Color(0xFFFF0000.toInt()) width=40.dp />
            <SizedRectangle color=Color(0xFF00FF00.toInt()) width=50.dp />
            <SizedRectangle color=Color(0xFF0000FF.toInt()) width=60.dp />
        }
        expanded(flex=1f) {
            <SizedRectangle color=Color(0xFF00FF00.toInt()) />
        }
    </FlexRow>
}

@Composable
fun FlexColumnUsage() {
    <FlexColumn>
        expanded(flex=2f) {
            <Center>
                <SizedRectangle color=Color(0xFF0000FF.toInt()) width=40.dp height=40.dp />
            </Center>
            <SizedRectangle color=Color(0xFF0000FF.toInt()) width=40.dp />
        }
        inflexible {
            <SizedRectangle color=Color(0xFFFF0000.toInt()) height=40.dp />
            <SizedRectangle color=Color(0xFF00FF00.toInt()) height=50.dp />
            <SizedRectangle color=Color(0xFF0000FF.toInt()) height=60.dp />
        }
        expanded(flex=1f) {
            <SizedRectangle color=Color(0xFF00FF00.toInt()) />
        }
    </FlexColumn>
}

@Composable
fun RowUsage() {
    <Row>
        <SizedRectangle color=Color(0xFF0000FF.toInt()) width=40.dp height=40.dp />
        <SizedRectangle color=Color(0xFFFF0000.toInt()) width=40.dp height=80.dp />
        <SizedRectangle color=Color(0xFF00FF00.toInt()) width=80.dp height=70.dp />
    </Row>
}

@Composable
fun ColumnUsage() {
    <Column>
        <SizedRectangle color=Color(0xFF0000FF.toInt()) width=40.dp height=40.dp />
        <SizedRectangle color=Color(0xFFFF0000.toInt()) width=40.dp height=80.dp />
        <SizedRectangle color=Color(0xFF00FF00.toInt()) width=80.dp height=70.dp />
    </Column>
}

@Composable
fun AlignUsage() {
    <Align alignment=Alignment.BottomRight>
        <SizedRectangle color=Color(0xFF0000FF.toInt()) width=40.dp height=40.dp />
    </Align>
}

@Composable
fun StackUsage() {
    <Stack defaultAlignment=Alignment.BottomRight> children ->
        children.aligned(Alignment.Center) {
            <SizedRectangle color=Color(0xFF0000FF.toInt()) width=300.dp height=300.dp />
        }
        children.aligned(Alignment.TopLeft) {
            <SizedRectangle color=Color(0xFF00FF00.toInt()) width=150.dp height=150.dp />
        }
        children.aligned(Alignment.BottomRight) {
            <SizedRectangle color=Color(0xFFFF0000.toInt()) width=150.dp height=150.dp />
        }
        // TODO(popam): insets should be named arguments
        children.positioned(null, 20.dp, null, 20.dp) {
            <SizedRectangle color=Color(0xFFFFA500.toInt()) width=80.dp />
            <SizedRectangle color=Color(0xFFA52A2A.toInt()) width=20.dp />
        }
        children.positioned(40.dp, null, null, null) {
            <SizedRectangle color=Color(0xFFB22222.toInt()) width=20.dp />
        }
        children.positioned(null, null, 40.dp, null) {
            <SizedRectangle color=Color(0xFFFFFF00) width=40.dp />
        }
    </Stack>
}

@Composable
fun ConstrainedBoxUsage() {
    <Align alignment=Alignment.Center>
        <ConstrainedBox additionalConstraints=Constraints.tightConstraints(50.dp, 50.dp)>
            <SizedRectangle color=Color(0xFFFF0000) />
        </ConstrainedBox>
    </Align>
}

fun PaddingUsage() {
    <Row>
        <Padding padding=EdgeInsets(20.dp)>
            <SizedRectangle color=Color(0xFFFF0000.toInt()) width=20.dp height=20.dp />
        </Padding>
        <Padding padding=EdgeInsets(20.dp)>
            <SizedRectangle color=Color(0xFFFF0000.toInt()) width=20.dp height=20.dp />
        </Padding>
    </Row>
}

@Composable
fun ContainerUsage() {
    <Align alignment=Alignment.Center>
        <Container
            color=Color(0xFF0000FF.toInt())
            alignment=Alignment.BottomRight
            width=100.dp
            height=100.dp
            margin=EdgeInsets(20.dp)>
            <Container
                padding=EdgeInsets(20.dp)
                color=Color(0xFF000000.toInt())
                alignment=Alignment.BottomRight
                width=50.dp
                height=50.dp>
                <SizedRectangle color=Color(0xFFFFFFFF.toInt()) />
            </Container>
        </Container>
    </Align>
}

@Composable
fun RowWithCrossAxisAlignmentUsage() {
    <Center>
        <Row crossAxisAlignment=CrossAxisAlignment.Start>
            <Container color=Color(0xFF00FF00.toInt()) width=50.dp height=50.dp/>
            <Container color=Color(0xFF0000FF.toInt()) width=80.dp height=80.dp/>
            <Container color=Color(0xFFFF0000.toInt()) width=70.dp height=70.dp/>
            <Container color=Color(0xFF00FF00.toInt()) width=100.dp height=100.dp/>
            <Container color=Color(0xFF0000FF.toInt()) width=20.dp height=20.dp/>
        </Row>
    </Center>
}

@Composable
fun RowWithMainAxisAlignmentUsage() {
    <Center>
        <Row mainAxisAlignment=MainAxisAlignment.SpaceBetween>
            <Container color=Color(0xFF00FF00.toInt()) width=50.dp height=50.dp/>
            <Container color=Color(0xFF0000FF.toInt()) width=80.dp height=80.dp/>
            <Container color=Color(0xFFFF0000.toInt()) width=70.dp height=70.dp/>
            <Container color=Color(0xFF00FF00.toInt()) width=100.dp height=100.dp/>
            <Container color=Color(0xFF0000FF.toInt()) width=20.dp height=20.dp/>
        </Row>
    </Center>
}

@Composable
fun RowWithMainAxisSizeUsage() {
    <Center>
        <Row mainAxisSize=MainAxisSize.Min mainAxisAlignment=MainAxisAlignment.SpaceBetween>
            <Container color=Color(0xFF00FF00.toInt()) width=50.dp height=50.dp/>
            <Container color=Color(0xFF0000FF.toInt()) width=80.dp height=80.dp/>
            <Container color=Color(0xFFFF0000.toInt()) width=70.dp height=70.dp/>
            <Container color=Color(0xFF00FF00.toInt()) width=100.dp height=100.dp/>
            <Container color=Color(0xFF0000FF.toInt()) width=20.dp height=20.dp/>
        </Row>
    </Center>
}

@Composable
fun SingleCompositionRow(@Children children: () -> Unit) {
    <Layout layoutBlock = { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(constraints.copy(minWidth = 0.ipx, maxWidth = IntPx.Infinity))
        }
        val width = placeables.fold(0.ipx) { sum, placeable -> sum + placeable.width }
        val height = placeables.fold(0.ipx) { max, placeable -> max(max, placeable.height) }

        layout(width, height) {
            var left = 0.ipx
            placeables.forEach { placeable ->
                placeable.place(left, 0.ipx)
                left += placeable.width
            }
        }
    } children />
}

@Composable
fun SingleCompositionColumn(@Children children: () -> Unit) {
    <Layout layoutBlock = { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(constraints.copy(minHeight = 0.ipx, maxHeight = IntPx.Infinity))
        }
        val width = placeables.fold(0.ipx) { max, placeable -> max(max, placeable.width) }
        val height = placeables.fold(0.ipx) { sum, placeable -> sum + placeable.height }

        layout(width, height) {
            var top = 0.ipx
            placeables.forEach { placeable ->
                placeable.place(0.ipx, top)
                top += placeable.height
            }
        }
    } children />
}

@Composable
fun SingleCompositionRect() {
    val Rectangle = @Composable {
            <Draw> canvas, parentSize ->
                val paint = Paint().apply { this.color = rectColorModel.color }
                canvas.drawRect(parentSize.toRect(), paint)
            </Draw>
        }
    <Layout layoutBlock = { measurables, constraints ->
        layout(100.ipx, 100.ipx) {}
    } children=Rectangle />
}

@Model
class RectColor(var color: Color = Color(0xFF00FF00.toInt()), var cnt: Int = 4)
val rectColorModel = RectColor()

@Composable
fun SingleCompositionRectWithIntrinsics() {
    val Rectangle = @Composable {
        <Draw> canvas, parentSize ->
            val paint = Paint().apply { this.color = rectColorModel.color }
            canvas.drawRect(parentSize.toRect(), paint)
        </Draw>
    }
    <ComplexLayout
        layoutBlock={ _, constraints ->
            layoutResult(50.ipx, 50.ipx) {}
        }
        minIntrinsicWidthBlock={ _, _ -> 50.ipx }
        maxIntrinsicWidthBlock={ _, _ -> 50.ipx }
        minIntrinsicHeightBlock={ _, _ -> 50.ipx }
        maxIntrinsicHeightBlock={ _, _ -> 50.ipx }
        children=Rectangle />
}

@Composable
fun SingleCompositionRowWithIntrinsics(@Children children: () -> Unit) {
    <ComplexLayout
        layoutBlock={ measurables, constraints ->
            val placeables = measurables.map { measurable ->
                val childWidth = measurable.maxIntrinsicWidth(constraints.maxHeight)
                measurable.measure(Constraints(
                    childWidth, childWidth, 0.ipx, constraints.maxHeight
                ))
            }
            val width = placeables.map { it.width }.sum()
            val height = placeables.map { it.height }.max()

            layoutResult(width, height) {
                var left = 0.ipx
                placeables.forEach { placeable ->
                    placeable.place(left, 0.ipx)
                    left += placeable.width
                }
            }
        }
        minIntrinsicWidthBlock={ measurables, h ->
            measurables.map { it.minIntrinsicWidth(h) }.sum()
        }
        maxIntrinsicWidthBlock={ measurables, h ->
            measurables.map { it.maxIntrinsicWidth(h) }.sum()
        }
        minIntrinsicHeightBlock={ measurables, w ->
            measurables.map { it.minIntrinsicHeight(w) }.max()
        }
        maxIntrinsicHeightBlock={ measurables, w ->
            measurables.map { it.maxIntrinsicHeight(w) }.max()
        }
        children />
}

@Composable
fun ComplexLayoutDemos() {
    +runDelayed(3000) {
        rectColorModel.color = Color(0xFF0000FF.toInt())
        rectColorModel.cnt++
    }
    <CraneWrapper>
        <Padding padding=EdgeInsets(50.dp)>
            <SingleCompositionRect />
        </Padding>
    </CraneWrapper>
}

fun runDelayed(millis: Int, block: () -> Unit) = effectOf<Unit> {
    val handler = +memo { Handler() }
    +onCommit {
        val runnable = object : Runnable {
            override fun run() {
                block()
            }
        }
        handler.postDelayed(runnable, millis.toLong())
        onDispose {
            handler.removeCallbacks(runnable)
        }
    }
}

fun Collection<IntPx>.sum(): IntPx {
    var result = 0.ipx
    for (item in this) {
        result += item
    }
    return result
}

fun Collection<IntPx>.max(): IntPx {
    var result = 0.ipx
    for (item in this) {
        result = max(result, item)
    }
    return result
}