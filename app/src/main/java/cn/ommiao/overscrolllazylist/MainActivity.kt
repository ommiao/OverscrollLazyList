package cn.ommiao.overscrolllazylist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlin.math.roundToInt

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
                    val navigationBarHeight = with(LocalDensity.current) {
                        insets.navigationBars.bottom.toDp()
                    }
                    val topBarOffset = remember {
                        mutableStateOf(0f)
                    }
                    BoxWithConstraints {
                        val screenHeight = maxHeight
                        OverscrollLazyColumn(
                            modifier = Modifier.navigationBarsPadding(),
                            columnContainerHeight = screenHeight,
                            maxOverscrollHeight = 100.dp,
                            onOverscrollHeightChange = {
                                topBarOffset.value = it
                            }
                        ) {
                            item {
                                Image(
                                    painter = painterResource(id = R.mipmap.puppy3),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(456.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            items(itemsList) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    backgroundColor = item.color.toColor(),
                                    shape = RoundedCornerShape(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = item.title, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    Card(
                        backgroundColor = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier
                            .padding(top = statusBarHeight)
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp)
                            .offset { IntOffset(x = 0, y = topBarOffset.value.roundToInt()) },
                        shape = RoundedCornerShape(28.dp)
                    ) {
                    }
                }
            }
        }
    }
}

val statusBarColor = Color(0xFFF1F1F1)

@Composable
private fun MyAppBar(statusBarHeight: Dp) {
    TopAppBar(
        contentPadding = PaddingValues(top = statusBarHeight),
        backgroundColor = statusBarColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Android")
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
