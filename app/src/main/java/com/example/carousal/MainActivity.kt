@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.carousal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.appversal.appstorys.AppStorys.appstorys
import com.appversal.appstorys.ui.OverlayContainer
import com.example.carousal.ui.theme.CarousalTheme


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

    LaunchedEffect(screenName) {
        if (screenName.isNotEmpty() && currentScreen != screenName) {
            currentScreen = screenName
            app.resetNavigation()
        }
    }
    campaignManager.getScreenCampaigns(
        "Home Screen",
        listOf()
    )

    var edgeToEdgePadding by remember { mutableStateOf(PaddingValues()) }

    var selectedTab by remember { mutableStateOf(0) } // Track selected tab index

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
            if (currentScreen == "PayScreen") {
                PayScreen(innerPadding)
            } else {
                when (selectedTab) {
                    0 -> HomeScreen(innerPadding)
                    1 -> PayScreen(innerPadding)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val campaignManager = App.appStorys

    val screenName  = "Home Screen"

    var showBottomSheet by remember { mutableStateOf(false) }

    campaignManager.getScreenCampaigns(
        screenName,
        listOf("widget_one", "widget_two", "widget_three", "widget_four", "widget_fifty"),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf1f2f4))
    ) {
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
                Box(modifier = Modifier.height(20.dp))

                campaignManager.Widget(
                    modifier = Modifier.appstorys("tooltip_home")
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
                            showBottomSheet = true
                            campaignManager.trackEvents(
                                event = "Button clicked"
                            )
                        },
                        modifier = Modifier.appstorys("open_bottom_sheet")
                    ) {
                        Text("Open Bottom Sheet")
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.home_two),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxWidth().appstorys("app_logo"),
                    contentScale = ContentScale.Fit
                )

            }
        }

        if (showBottomSheet) {
            campaignManager.BottomSheet(
                onDismissRequest = { showBottomSheet = false },
            )
        }

        OverlayContainer.Content(
            topPadding = 70.dp,
            bottomPadding = 70.dp
        )
    }
}

@Composable
fun PayScreen(padding: PaddingValues) {

    val context = LocalContext.current
    val campaignManager = App.appStorys

    campaignManager.getScreenCampaigns(
        "More Screen",
        listOf()
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
            .background(Color(0xFFf1f2f4)),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.more_one),
                        contentDescription = "App Logo",
                        modifier = Modifier.weight(1f).appstorys("cashbook"),
                        contentScale = ContentScale.Fit
                    )

                    Image(
                        painter = painterResource(id = R.drawable.more_two),
                        contentDescription = "App Logo",
                        modifier = Modifier.weight(1f).appstorys("bills"),
                        contentScale = ContentScale.Fit
                    )

                    Image(
                        painter = painterResource(id = R.drawable.more_three),
                        contentDescription = "App Logo",
                        modifier = Modifier.weight(1f).appstorys("items"),
                        contentScale = ContentScale.Fit
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.more_bottom),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    OverlayContainer.Content(
        topPadding = 70.dp,
        bottomPadding = 70.dp
    )

}

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
