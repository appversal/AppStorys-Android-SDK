@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.carousal

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appversal.appstorys.ui.OverlayContainer
import com.appversal.appstorys.utils.appstorys
import com.example.carousal.ui.theme.CarousalTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import kotlinx.coroutines.coroutineScope


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarousalTheme {
                Box {
                    MyApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    val context = LocalContext.current
    val campaignManager = App.appStorys
    val app = LocalContext.current.applicationContext as App
    val screenName by app.screenNameNavigation.collectAsState()
    var currentScreen by remember { mutableStateOf("HomeScreen") }

    var selectedTab by remember { mutableStateOf(0) } // Track selected tab index

    LaunchedEffect(screenName) {
        if (screenName.isNotEmpty()) {
            when (screenName) {
                "PayScreen" -> {
                    selectedTab = 1 // Set to PayScreen tab
                    currentScreen = "HomeScreen" // Keep normal navigation
                }
                "HomeScreen" -> {
                    selectedTab = 0
                    currentScreen = "HomeScreen"
                }
                else -> {
                    currentScreen = screenName // For other screens
                }
            }
            app.resetNavigation()
        }
    }
//    campaignManager.getScreenCampaigns(
//        "Home Screen",
//        listOf()
//    )

    var edgeToEdgePadding by remember { mutableStateOf(PaddingValues()) }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFFFAF8F9),

            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.topbar),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .height(56.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0752ad),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                )
            },


            bottomBar = {
                BottomNavigationBar(selectedTab) { newIndex -> selectedTab = newIndex }
            }
        ) { innerPadding ->
            edgeToEdgePadding = innerPadding
//            if (currentScreen == "PayScreen") {
//                PayScreen(innerPadding)
//            } else {
                when (selectedTab) {
                    0 -> HomeScreen(innerPadding)
                    1 -> PayScreen(innerPadding)
                }
//            }
        }

        App.appStorys.overlayElements(
            topPadding = 70.dp,
            bottomPadding = 70.dp,
        )
    }
}

@Composable
fun CopyUserIdText() {
    val campaignManager = App.appStorys
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val userId = campaignManager.getUserId()

    Text(
        text = userId,
        modifier = androidx.compose.ui.Modifier.clickable {
            clipboardManager.setText(AnnotatedString(userId))
            Toast.makeText(context, "User ID copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
fun HomeScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val campaignManager = App.appStorys

    LaunchedEffect(Unit) {
        val screenName  = "Home Screen"
        val positions = listOf("widget_one", "widget_two", "widget_three", "widget_four", "widget_fifty")
        Log.i("Positions", "About to call getScreenCampaigns with: $positions")
        campaignManager.getScreenCampaigns(
            screenName,
            positions,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf1f2f4))
    ) {
        val coroutineScope = rememberCoroutineScope()
        // Scrollable Column using LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .appstorys("lazy_column")
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ), // Add this line,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.home_one),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxWidth(),
//                        .clickable { showBottomSheet = true },
                    contentScale = ContentScale.Fit
                )

                CopyUserIdText()

                campaignManager.Widget(
                    modifier = Modifier.appstorys("tooltip_home"),
//                    position = null
                )

                campaignManager.Widget(
                    modifier = Modifier.fillMaxWidth().appstorys("tooltip_home_prem_test"),
                    placeholder = context.getDrawable(R.drawable.ic_launcher_foreground),
                    position = "widget_one",
                )

                campaignManager.Widget(
                    modifier = Modifier.fillMaxWidth().appstorys("tooltip_home_prem_test"),
                    placeholder = context.getDrawable(R.drawable.ic_launcher_foreground),
                    position = "widget_two",
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Button(
                        onClick = {
//                            campaignManager.trackEvents(
//                                event = "Button clicked"
//                            )
                                campaignManager.setUserProperties(mapOf("test" to "prem"))
                        },
                        modifier = Modifier.appstorys("open_bottom_sheet")
                    ) {
                        Text("Open Bottom Sheet")
                    }
                }

                Button(
                    onClick = {
                        campaignManager.trackEvents(
                            event = "Login"
                        )
//                        campaignManager.trackEvents(
//                            event = "Logout",
//                            // optional metadata = mapOf()
//                        )
                    },
                    modifier = Modifier
                ) {
                    Text("Login Event")
                }

                Button(
                    onClick = {
                        campaignManager.trackEvents(
                            event = "Added to cart"
                        )
                    },
                    modifier = Modifier
                ) {
                    Text("Added to cart Event")
                }

                Button(
                    onClick = {
                        campaignManager.trackEvents(
                            event = "Purchased"
                        )
                    },
                    modifier = Modifier
                ) {
                    Text("Purchased Event")
                }

                Button(
                    onClick = {
                        campaignManager.trackEvents(
                            event = "Logout",
                            // optional metadata = mapOf()
                        )
                    },
                    modifier = Modifier
                ) {
                    Text("Logout Event")
                }

                Button(
                    onClick = {
                        campaignManager.trackEvents(
                            event = "AppStorys Success"
                        )
                    },
                    modifier = Modifier
                ) {
                    Text("AppStorys Success Event")
                }

                campaignManager.Stories()

                campaignManager.Reels()

                Image(
                    painter = painterResource(id = R.drawable.home_two),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxWidth().appstorys("app_logo"),
                    contentScale = ContentScale.Fit
                )

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PayScreen(padding: PaddingValues) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
            .background(Color(0xFFf1f2f4)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NavigationButton(
                    text = "Cashbook",
                    isSelected = pagerState.currentPage == 0,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )

                NavigationButton(
                    text = "Bills",
                    isSelected = pagerState.currentPage == 1,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )

                NavigationButton(
                    text = "Items",
                    isSelected = pagerState.currentPage == 2,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    }
                )
            }

            // Horizontal pager for screens
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> PayScreenPage(
                        topImages = listOf(
                            Triple(R.drawable.more_one, "cashbook", "Cashbook"),
                            Triple(R.drawable.more_two, "bills", "Bills"),
                            Triple(R.drawable.more_three, "items", "Items")
                        ),
                        bottomImage = R.drawable.more_bottom,
                        buttonText = "Cashbook Tab",
//                        campaignManager = campaignManager,
                        screenType = "cashbook"
                    )

                    1 -> PayScreenPage(
                        topImages = listOf(
                            Triple(R.drawable.more_one, "cashbook", "Cashbook"),
                            Triple(R.drawable.more_three, "items", "Items"),
                            Triple(R.drawable.more_two, "bills", "Bills")
                        ),
                        bottomImage = R.drawable.more_bottom,
                        buttonText = "Bills Tab",
//                        campaignManager = campaignManager,
                        screenType = "bills"
                    )

                    2 -> PayScreenPage(
                        topImages = listOf(
                            Triple(R.drawable.more_three, "items", "Items"),
                            Triple(R.drawable.more_one, "cashbook", "Cashbook"),
                            Triple(R.drawable.more_two, "bills", "Bills")
                        ),
                        bottomImage = R.drawable.more_bottom,
                        buttonText = "Items Tab",
                        screenType = "items"
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .padding(horizontal = 4.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun PayScreenPage(
    topImages: List<Triple<Int, String, String>>, // resourceId, appstorys tag, contentDescription
    bottomImage: Int,
    buttonText: String,
    screenType: String
) {
    val campaignManager = App.appStorys
    val imageTags = topImages.map { it.second }

    LaunchedEffect(buttonText) {
        campaignManager.getScreenCampaigns(
            buttonText,
            listOf()
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top row with three images
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                topImages.forEach { (imageRes, tag, description) ->
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = description,
                        modifier = Modifier
                            .weight(1f)
                            .appstorys(tag),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom image
            Image(
                painter = painterResource(id = bottomImage),
                contentDescription = "Bottom Image",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action button
            Button(
                onClick = {
                },
                modifier = Modifier

                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(buttonText)
            }

            Text(buttonText)
        }
    }

}

//@Composable
//fun PayScreen(padding: PaddingValues) {
//
//    val campaignManager = App.appStorys
//
//    campaignManager.getScreenCampaigns(
//        "More Screen",
//        listOf()
//    )
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
//            .background(Color(0xFFf1f2f4)),
//        contentAlignment = Alignment.TopCenter
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//            ) {
//                Row {
//                    Image(
//                        painter = painterResource(id = R.drawable.more_one),
//                        contentDescription = "App Logo",
//                        modifier = Modifier.weight(1f).appstorys("cashbook"),
//                        contentScale = ContentScale.Fit
//                    )
//
//                    Image(
//                        painter = painterResource(id = R.drawable.more_two),
//                        contentDescription = "App Logo",
//                        modifier = Modifier.weight(1f).appstorys("bills"),
//                        contentScale = ContentScale.Fit
//                    )
//
//                    Image(
//                        painter = painterResource(id = R.drawable.more_three),
//                        contentDescription = "App Logo",
//                        modifier = Modifier.weight(1f).appstorys("items"),
//                        contentScale = ContentScale.Fit
//                    )
//                }
//                Image(
//                    painter = painterResource(id = R.drawable.more_bottom),
//                    contentDescription = "App Logo",
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    contentScale = ContentScale.Fit
//                )
//
//                Button(
//                    onClick = {
//                        campaignManager.trackEvents(
//                            event = "clicked"
//                        )
//                    },
//                    modifier = Modifier
//                ) {
//                    Text("Clicked Event")
//                }
//            }
//        }
//    }
//    campaignManager.overlayElements(
//        topPadding = 70.dp,
//        bottomPadding = 70.dp,
//    )
//}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White, // Add this line to set the background color to white
        modifier = Modifier.fillMaxWidth().height(70.dp)

    ) {
        val items = listOf("Parties", "More")
        val icons = listOf(Icons.Filled.Person, Icons.Filled.List)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround // Adjust spacing here
        ) {
            items.forEachIndexed { index, title ->
                NavigationBarItem(
//                    modifier = if (index == 0) Modifier.appstorys("tooltip_home") else Modifier,
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    icon = {
                        Icon(
                            modifier = Modifier.size(24.dp), // Apply modifier from ToolTipWrapper
                            imageVector = icons[index],
                            contentDescription = title,
                            tint = if (selectedTab == index) Color(0xFF186fd9) else Color.Gray
                        )
//                        }
                    },
                    label = {
                        Text(
                            title,
                            color = if (selectedTab == index) Color(0xFF186fd9) else Color.Gray
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF01C198),
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color.Transparent // Remove default background
                    )
                )
            }
        }
    }
}
