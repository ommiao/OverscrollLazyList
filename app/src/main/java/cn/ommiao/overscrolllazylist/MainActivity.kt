package cn.ommiao.overscrolllazylist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import cn.ommiao.dragtodelete.extension.toColor
import cn.ommiao.overscrolllazylist.data.itemsList
import cn.ommiao.overscrolllazylist.ui.theme.OverviewLazyListTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

val rowItemSize = 56.dp
val columnItemHeight = 200.dp
val columnHeaderHeight = 456.dp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SystemUiController()
            OverviewLazyListTheme {
                ProvideWindowInsets {
                    val insets = LocalWindowInsets.current
                    val statusBarHeight = with(LocalDensity.current) {
                        insets.statusBars.top.toDp()
                    }
                    val topBarOffset = remember {
                        mutableStateOf(0f)
                    }
                    val scrollState = rememberLazyListState()
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val screenHeight = maxHeight
                        OverscrollLazyColumn(
                            state = scrollState,
                            modifier = Modifier.navigationBarsPadding(),
                            maxOverscrollHeight = 100.dp,
                            onOverscrollHeightChange = {
                                topBarOffset.value = it
                            },
                            overscrollContent = {
                                OverscrollContent()
                            }
                        ) {
                            items(itemsList) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(columnItemHeight),
                                    backgroundColor = item.color.toColor(),
                                    shape = RoundedCornerShape(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.title,
                                            color = Color.White,
                                            style = MaterialTheme.typography.body2
                                        )
                                    }
                                }
                            }
                        }
                        TopBar(statusBarHeight, topBarOffset)
                        BottomBar(scrollState, screenHeight)
                    }
                }
            }
        }
    }

    @Composable
    private fun BoxScope.BottomBar(scrollState: LazyListState, screenHeight: Dp) {
        val scope = rememberCoroutineScope()
        Card(
            backgroundColor = Color.White.copy(alpha = 0.88f),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .align(Alignment.BottomCenter)
        ) {
            Row {
                Image(
                    painter = painterResource(id = R.mipmap.puppy3),
                    contentDescription = "puppy-mini",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .size(rowItemSize)
                        .clickable {
                            scope.launch {
                                scrollState.animateScrollToItem(0)
                            }
                        }
                )
                Divider(
                    Modifier
                        .padding(vertical = 16.dp)
                        .height(44.dp)
                        .width(1.dp)
                        .align(Alignment.CenterVertically)
                )
                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(itemsList) {
                        val offset = with(LocalDensity.current) {
                            -((screenHeight - columnItemHeight) / 2).toPx().roundToInt()
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(it.color.toColor())
                                .size(rowItemSize)
                                .clickable {
                                    scope.launch {
                                        scrollState.animateScrollToItem(
                                            itemsList.indexOf(it) + 1,
                                            offset
                                        )
                                    }
                                }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun OverscrollContent() {
        Box {
            Image(
                painter = painterResource(id = R.mipmap.puppy3),
                contentDescription = "puppy-top",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(columnHeaderHeight),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "A little dog",
                color = Color.White,
                style = MaterialTheme.typography.h3,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun TopBar(
        statusBarHeight: Dp,
        topBarOffset: MutableState<Float>
    ) {
        Card(
            backgroundColor = Color.White.copy(alpha = 0.88f),
            modifier = Modifier
                .padding(top = statusBarHeight)
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp)
                .offset { IntOffset(x = 0, y = topBarOffset.value.roundToInt()) },
            shape = RoundedCornerShape(28.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Nothing to show", style = MaterialTheme.typography.body1)
            }
        }
    }
}

@Composable
private fun SystemUiController() {
    val systemUiController = rememberSystemUiController()
    val darkIcons = false
    SideEffect {
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = darkIcons)
    }
}
