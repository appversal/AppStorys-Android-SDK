package com.example.carousal

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
        listOf("widget_one", "widget_three", "widget_fifty", "widget_four"),
        listOf("button_one", "button_two")
    )

    var edgeToEdgePadding by remember { mutableStateOf(PaddingValues()) }

    var selectedTab by remember { mutableStateOf(0) } // Track selected tab index

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFFFAF8F9),

            bottomBar = {
                BottomNavigationBar(selectedTab) { newIndex -> selectedTab = newIndex }
            }
//            bottomBar = {
//
////                    campaignManager.ToolTipWrapper(
////                        targetModifier = Modifier,
////                        targetKey = "about_button",
////                    ) {
////                        Button(modifier = it, onClick = {}) { }
////                    }
//
//            }
        ) { innerPadding ->
            edgeToEdgePadding = innerPadding
            if (currentScreen == "PayScreen"){
                PayScreen(innerPadding)
            }

            else{
                when (selectedTab) {
                    0 -> HomeScreen(innerPadding)
                    1 -> PayScreen(innerPadding)
                    2 -> UtilityScreen()
                    3 -> RewardScreen()
                }
            }
        }

//        campaignManager.Pip()

    }


}

@Composable
fun HomeScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val campaignManager = App.appStorys

    campaignManager.getScreenCampaigns(
        "Home Screen",
        listOf("widget_one", "widget_two", "widget_three", "widget_four", "widget_fifty"),
        listOf("wizor", "button_one", "button_two", "button_three", "button_four")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F8)) // Optional background color
    ) {
        // Scrollable Column using LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()), // Add this line,
            horizontalAlignment = Alignment.CenterHorizontally, // Center align items horizontally
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly // Distributes items evenly
                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.home_top_one),
//                        contentDescription = "Home Screen Top Image",
//                        modifier = Modifier
//                            .weight(1f) // Distributes space evenly
//                            .height(80.dp), // Maintain height
//                        contentScale = ContentScale.Fit // Keeps the aspect ratio intact
//                    )

                    Column(

                    ) {
                        Spacer(Modifier.height(24.dp))
//                        campaignManager.ToolTipWrapper(
//                            targetModifier = Modifier,
//                            targetKey = "wizor"
//                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.icon),
//                                contentDescription = "Home Screen Top Image",
//                                modifier = it
//                                    .weight(0.35f) // Adjust weight for smaller icon
//                                    .height(60.dp), // Set appropriate height
////                                .offset(y = 25.dp), // Moves the image down without resizing
//                                contentScale = ContentScale.Fit
//                            )
//                        }
                    }

//                    Image(
//                        painter = painterResource(id = R.drawable.home_top_two),
//                        contentDescription = "Home Screen Top Image",
//                        modifier = Modifier
//                            .weight(1.3f) // Ensures balance
//                            .height(80.dp),
//                        contentScale = ContentScale.Fit
//                    )
                }


                campaignManager.Widget(
                    modifier = Modifier,
                    placeHolder = context.getDrawable(R.drawable.ic_launcher_foreground),
                    position = "widget_one"
                )

                campaignManager.Widget(
                    modifier = Modifier,
                    placeHolder = context.getDrawable(R.drawable.ic_launcher_foreground),
                    position = "widget_two"
                )

                campaignManager.ToolTipWrapper(
                    targetModifier = Modifier,
                    targetKey = "wizor"
                ) {
                    Button(
                        modifier = it,
                        onClick = {}
                    ) {
                        Text("Button")
                    }
                }



//                Spacer(modifier = Modifier.height(12.dp))

                campaignManager.Widget(
                    modifier = Modifier.fillMaxWidth(),
                    placeHolder = context.getDrawable(R.drawable.ic_launcher_foreground),
                    position = "widget_three"
                )

                Spacer(modifier = Modifier.height(28.dp))

                campaignManager.Widget(
                    modifier = Modifier.fillMaxWidth(),
                    placeHolder = context.getDrawable(R.drawable.ic_launcher_foreground),
                    position = "widget_fifty"
                )

//                Spacer(modifier = Modifier.height(12.dp))

                campaignManager.Widget(
                    modifier = Modifier.fillMaxWidth(),
                    placeHolder = context.getDrawable(R.drawable.ic_launcher_foreground),
                    position = "widget_four"
                )

//                Spacer(modifier = Modifier.height(80.dp))
            }
        }

//        campaignManager.PinnedBanner(
//            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
//            placeHolder = context.getDrawable(R.drawable.ic_launcher_foreground),
////            position = "banner_bottom"
//            position = null
//        )
    }
}

@Composable
fun PayScreen(padding: PaddingValues) {

    val context = LocalContext.current
    val campaignManager = App.appStorys

//    var isSheetOpen by remember { mutableStateOf(false) }

    campaignManager.getScreenCampaigns(
        "Pay Screen",
        listOf("banner_bottom"),
        listOf()
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = padding.calculateBottomPadding())
            .background(Color.White),
        contentAlignment = Alignment.TopCenter
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Enables scrolling
        ){
//            Image(
//                painter = painterResource(id = R.drawable.pay_screen_top), // Use the image from drawable
//                contentDescription = "Pay Screen Image",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .wrapContentHeight(),
//                contentScale = ContentScale.FillWidth // Ensures the image fits the screen width
//            )
//
//            Image(
//                painter = painterResource(id = R.drawable.pay_screen_bottom), // Use the image from drawable
//                contentDescription = "Pay Screen Image",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .wrapContentHeight(),
////                    .clickable { isSheetOpen = true },
//                contentScale = ContentScale.FillWidth // Ensures the image fits the screen width
//            )


        }

//        if (isSheetOpen) {
//            BottomSheetComponent(onDismiss = { isSheetOpen = false })
//        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            campaignManager.PinnedBanner(
                modifier = Modifier.fillMaxWidth(),
                placeHolder = context.getDrawable(R.drawable.ic_launcher_foreground),
//                placeholderContent = {}
//            position = "banner_bottom"
                position = null
            )
        }
    }
}

@Composable
fun UtilityScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Centers content inside the Box
    ) {
        Text(
            text = "Utilities Screen",
            fontSize = 18.sp
        )
    }
}

@Composable
fun RewardScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Centers content inside the Box
    ) {
        Text(
            text = "Rewards Screen",
            fontSize = 18.sp
        )
    }
}

//@Composable
//fun AllCampaigns() {
//    val campaignManager = App.appStorys
//    val context = LocalContext.current
//
//    val bannerHeight = campaignManager.getBannerHeight()
//
//    Log.i("BannerHeight", bannerHeight.toString())
////    var showPip by remember { mutableStateOf(true) }
//    Box {
//        Column(modifier = Modifier.align(Alignment.Center)){
//            campaignManager.PinnedBanner(modifier = Modifier, contentScale = ContentScale.FillWidth, placeHolder = context.getDrawable(R.drawable.ic_launcher_foreground), position = null)
//
//            campaignManager.Widget(modifier = Modifier, contentScale = ContentScale.FillWidth, placeHolder = context.getDrawable(R.drawable.ic_launcher_foreground), position = null)
//
//        }
//    }
//
//}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val campaignManager = App.appStorys
    NavigationBar (
        containerColor = Color.White, // Add this line to set the background color to white
        modifier = Modifier.fillMaxWidth()
    ){
        val items = listOf("Home", "Pay", "Utilities", "Rewards")
        val icons = listOf(Icons.Filled.Home, Icons.Filled.Person, Icons.Filled.List, Icons.Filled.ShoppingCart) // Use Material Icons
        val tooltipKeys = listOf("button_one", "button_two", "button_three", "button_four")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround // Adjust spacing here
        ){
            items.forEachIndexed { index, title ->
                campaignManager.ToolTipWrapper(
                    targetModifier = Modifier,
                    targetKey = tooltipKeys[index],
                    isNavigationBarItem = true
                ) {
                    NavigationBarItem(
                        modifier = it,
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp), // Apply modifier from ToolTipWrapper
                                imageVector = icons[index],
                                contentDescription = title,
                                tint = if (selectedTab == index) Color(0xFF01C198) else Color.Gray
                            )
//                        }
                        },
                        label = {
                            Text(
                                title,
                                color = if (selectedTab == index) Color(0xFF01C198) else Color.Gray
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
}




//@Composable
//fun PayScreen() {
//
//    Box {
//
//    }
//
//}

//@Preview(showBackground = true)
//@Composable
//fun MyAppPreview() {
//    CarousalTheme {
//        MyApp()
//    }
//}
