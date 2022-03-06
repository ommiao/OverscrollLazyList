package cn.ommiao.overscrolllazylist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.gestures.OverScrollConfiguration
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    overscrollContainerHeight: Dp,
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
        Box(modifier = modifier.nestedScroll(connection)) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                state = state,
                contentPadding = contentPadding,
                reverseLayout = reverseLayout,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                flingBehavior = flingBehavior,
                userScrollEnabled = userScrollEnabled,
                content = {
                    item {
                        val dynamicOffsetYDp = with(LocalDensity.current) {
                            dynamicOffsetY.value.toDp()
                        }
                        Box(
                            modifier = Modifier
                                .height(overscrollContainerHeight - maxOverscrollHeight + dynamicOffsetYDp)
                                .wrapContentHeight(align = Alignment.Bottom, unbounded = true)
                        ) {
                            Box(modifier = Modifier.requiredHeight(overscrollContainerHeight)) {
                                overscrollContent()
                            }
                        }
                    }
                    content()
                }
            )
        }
    }
}
