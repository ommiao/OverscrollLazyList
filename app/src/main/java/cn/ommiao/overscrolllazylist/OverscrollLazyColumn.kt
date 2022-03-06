package cn.ommiao.overscrolllazylist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.gestures.OverScrollConfiguration
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/*
   overscrollContent will be placed in the first item of LazyColumn
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverscrollLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    overScrollConfiguration: OverScrollConfiguration = OverScrollConfiguration(),
    maxOverscrollHeight: Dp,
    onOverscrollHeightChange: (Float) -> Unit = {},
    overscrollContent: @Composable () -> Unit,
    content: LazyListScope.() -> Unit
) {
    val dynamicOffsetY = remember {
        mutableStateOf(0f)
    }
    val maxOffsetY = with(LocalDensity.current) {
        maxOverscrollHeight.toPx()
    }
    val connection = remember {
        OverscrollNestedConnection(
            scrollState = state,
            maxOffsetY = maxOffsetY,
            onOverscrollHeightChange = {
                onOverscrollHeightChange(it)
                dynamicOffsetY.value = it
            }
        )
    }
    val scrollConfiguration =
        if (dynamicOffsetY.value > 0 || state.firstVisibleItemIndex != 0 || state.firstVisibleItemScrollOffset != 0) overScrollConfiguration else null
    CompositionLocalProvider(LocalOverScrollConfiguration provides scrollConfiguration) {
        LazyColumn(
            modifier = modifier.nestedScroll(connection),
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = {
                item {
                    Box(
                        modifier = Modifier.layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(
                                placeable.width,
                                (placeable.height - maxOffsetY + dynamicOffsetY.value).roundToInt()
                            ) {
                                placeable.place(
                                    0,
                                    (dynamicOffsetY.value - maxOffsetY).roundToInt()
                                )
                            }
                        }
                    ) {
                        overscrollContent()
                    }
                }
                content()
            }
        )
    }
}
