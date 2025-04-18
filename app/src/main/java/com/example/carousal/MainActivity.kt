@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.carousal

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carousal.ui.theme.CarousalTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarousalTheme {
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {
    val context = LocalContext.current
    val campaignManager = App.appStorys
    val app = LocalContext.current.applicationContext as App
    val screenName by app.screenNameNavigation.collectAsState()
    var currentScreen by remember { mutableStateOf("HomeScreen") }

    LaunchedEffect(screenName) {
        if (screenName.isNotEmpty() && currentScreen != screenName){
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
                                    .height(56.dp)
                                ,
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
            if (currentScreen == "PayScreen"){
                PayScreen(innerPadding)
            }

            else{
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

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    campaignManager.getScreenCampaigns(
        "Home Screen",
        listOf("widget_one", "widget_two", "widget_three", "widget_four", "widget_fifty"),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf1f2f4)) // Optional background color
    ) {
        // Scrollable Column using LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()), // Add this line,
            horizontalAlignment = Alignment.CenterHorizontally, // Center align items horizontally
        ) {
            item {

                Image(
                    painter = painterResource(id = R.drawable.home_one),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBottomSheet = true },
                    contentScale = ContentScale.Fit
                )

                Image(
                    painter = painterResource(id = R.drawable.home_two),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )

//                campaignManager.ToolTipWrapper(
//                    targetModifier = Modifier,
//                    targetKey = "premNewTooltipnew"
//                ) {
//                    Button(
//                        modifier = it,
//                        onClick = {}
//                    ) {
//                        Text("Button")
//                    }
//                }
//
//                campaignManager.Widget(
//                    modifier = Modifier.fillMaxWidth(),
//                    placeHolder = context.getDrawable(R.drawable.ic_launcher_foreground),
//                    position = "widget_three"
//                )
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                containerColor = Color.White,
                dragHandle = {
                    // Custom drag handle with padding to move it upward
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp) // This moves the drag handle upward
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        // The actual drag handle bar
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(
                                    color = Color.Gray.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column {
                        Spacer(Modifier.height(20.dp))
                        Image(
                            painter = painterResource(id = R.drawable.bottomsheet),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

        campaignManager.Pip(bottomPadding = padding.calculateBottomPadding(), topPadding = padding.calculateTopPadding())
        Box(
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ){
            campaignManager.CSAT()
        }
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
            .padding(top = padding.calculateTopPadding(),bottom = padding.calculateBottomPadding())
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
                    campaignManager.ToolTipWrapper(
                    targetModifier = Modifier,
                    targetKey = "cashbook"
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.more_one),
                            contentDescription = "App Logo",
                            modifier = it.weight(1f),
                            contentScale = ContentScale.Fit
                        )
                    }

                    campaignManager.ToolTipWrapper(
                        targetModifier = Modifier,
                        targetKey = "bills"
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.more_two),
                            contentDescription = "App Logo",
                            modifier = it.weight(1f),
                            contentScale = ContentScale.Fit
                        )
                    }

                    campaignManager.ToolTipWrapper(
                        targetModifier = Modifier,
                        targetKey = "items"
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.more_three),
                            contentDescription = "App Logo",
                            modifier = it.weight(1f),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Image(
                    painter = painterResource(id = R.drawable.more_bottom),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                    ,
                    contentScale = ContentScale.Fit
                )
                campaignManager.Widget(
                    modifier = Modifier.fillMaxWidth(),
                    placeHolder = context.getDrawable(R.drawable.ic_launcher_foreground),
                    position = null
                )
            }
//            campaignManager.PinnedBanner(
//                modifier = Modifier.fillMaxWidth(),
//                placeHolder = context.getDrawable(R.drawable.ic_launcher_foreground),
//                position = null
//            )
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar (
        containerColor = Color.White, // Add this line to set the background color to white
        modifier = Modifier.fillMaxWidth().height(70.dp)

    ){
        val items = listOf("Parties", "More")
        val icons = listOf(Icons.Filled.Person, Icons.Filled.List)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround // Adjust spacing here
        ){
            items.forEachIndexed { index, title ->
                    NavigationBarItem(
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
