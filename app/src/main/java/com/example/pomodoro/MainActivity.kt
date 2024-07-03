package com.example.pomodoro

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pomodoro.ui.theme.PomodoroTheme
import kotlinx.coroutines.delay

object Destinations {
    const val HOME_ROUTE = "Home"
    const val SETTINGS_ROUTE = "Settings"
}

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val onItemClick: () -> Unit
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PomodoroApp(isDarkTheme = isSystemInDarkTheme())
        }
    }
}

@Composable
fun PomodoroApp(isDarkTheme: Boolean) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var isDarkTheme by rememberSaveable {
        mutableStateOf(sharedPrefs.getBoolean("dark_theme", isDarkTheme))
    }

    PomodoroTheme(darkTheme = isDarkTheme) {
        val items = listOf(
            BottomNavigationItem(
                title = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                onItemClick = {
                    navController.navigate(Destinations.HOME_ROUTE)
                }
            ),
            BottomNavigationItem(
                title = "Settings",
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                onItemClick = {
                    navController.navigate(Destinations.SETTINGS_ROUTE)
                }
            )
        )
        var selectedItemIndex by rememberSaveable {
            mutableIntStateOf(0)
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isDarkTheme) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedItemIndex == index,
                                onClick = {
                                    selectedItemIndex = index
                                    when (index) {
                                        0 -> navController.navigate(Destinations.HOME_ROUTE)
                                        1 -> navController.navigate(Destinations.SETTINGS_ROUTE)
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (index == selectedItemIndex) {
                                            item.selectedIcon
                                        } else item.unselectedIcon,
                                        contentDescription = item.title
                                    )

                                })
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(navController, startDestination = Destinations.HOME_ROUTE) {
                    composable(Destinations.HOME_ROUTE) {
                        Home(isDarkTheme)
                    }
                    composable(Destinations.SETTINGS_ROUTE) {
                        Settings(isDarkTheme) { isDark ->
                            isDarkTheme = isDark
                            sharedPrefs.edit().putBoolean("dark_theme", isDarkTheme).apply()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Home(isDarkTheme: Boolean) {
    PomodoroTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            TimerScreen(isDarkTheme)
        }
    }
}

@Composable
fun TimerScreen(isDarkTheme: Boolean) {
    var timeInSeconds by remember { mutableStateOf(1500) } // Default to 25 minutes
    var isRunning by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning && timeInSeconds > 0) {
                delay(1000L)
                timeInSeconds--
            }
            isRunning = false
        }
    }

    // Use isDarkTheme directly for determining colors
    val backgroundColor = if (isDarkTheme) Color.DarkGray else Color.Gray

    val circleColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = if (isDarkTheme) Color.White else Color.Black,
        contentColor = if (isDarkTheme) Color.Black else Color.White
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
                .background(circleColor)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(timeInSeconds),
                    color = textColor,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row {
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = buttonColors
                    ) {
                        Text(text = if (isRunning) "Stop" else "Start")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            isRunning = false
                            timeInSeconds = 1500 // Reset to 25 minutes
                        },
                        colors = buttonColors
                    ) {
                        Text(text = "Reset")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { showDialog = true },
                    colors = buttonColors
                ) {
                    Text(text = "Set Timer Duration")
                }

                if (showDialog) {
                    TimerSettingsDialog(
                        currentDuration = timeInSeconds,
                        onDismiss = { showDialog = false },
                        onDurationChange = { newDuration ->
                            timeInSeconds = newDuration
                            showDialog = false
                            isRunning = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TimerSettingsDialog(currentDuration: Int, onDismiss: () -> Unit, onDurationChange: (Int) -> Unit) {
    var selectedMinutes by remember { mutableStateOf(currentDuration / 60) }
    var selectedSeconds by remember { mutableStateOf(currentDuration % 60) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Set Timer Duration", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NumberPicker(
                        label = "Minutes",
                        range = 0..59,
                        selectedValue = selectedMinutes,
                        onValueChange = {
                            selectedMinutes = it
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    NumberPicker(
                        label = "Seconds",
                        range = 0..59,
                        selectedValue = selectedSeconds,
                        onValueChange = {
                            selectedSeconds = it
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = { onDismiss() }) {
                        Text(text = "Cancel")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        onDurationChange(selectedMinutes * 60 + selectedSeconds)
                    }) {
                        Text(text = "Set")
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPicker(label: String, range: IntRange, selectedValue: Int, onValueChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.height(150.dp)
        ) {
            items(range.toList()) { value ->
                TextButton(
                    onClick = { onValueChange(value) },
                    enabled = value != selectedValue
                ) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (value == selectedValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@Composable
fun Settings(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    PomodoroTheme(darkTheme = isDarkTheme) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeChange(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSecondary,
                    )
                )
                Text(
                    text = if (isDarkTheme) "Dark Mode" else "Light Mode",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
