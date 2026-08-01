package com.salehsafary.darkcalc

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salehsafary.darkcalc.ui.theme.DarkCalcTheme

data class InstalledApp(
    val label: String,
    val packageName: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DarkCalcTheme {
                DarkCalcApp(
                    apps = getInstalledApps(),
                    onLaunchApp = ::launchApp
                )
            }
        }
    }

    private fun getInstalledApps(): List<InstalledApp> {
        val pm = packageManager

        return pm.getInstalledApplications(
            PackageManager.GET_META_DATA
        )
            .asSequence()
            .filter {
                pm.getLaunchIntentForPackage(it.packageName) != null
            }
            .map { appInfo ->
                InstalledApp(
                    label = pm.getApplicationLabel(appInfo).toString(),
                    packageName = appInfo.packageName
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)

        if (intent != null) {
            startActivity(intent)
        }
    }
}

@Composable
fun DarkCalcApp(
    apps: List<InstalledApp>,
    onLaunchApp: (String) -> Unit
) {
    var showDrawer by remember {
        mutableStateOf(false)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (showDrawer) {
            AppDrawer(
                apps = apps,
                onLaunchApp = onLaunchApp,
                onBack = {
                    showDrawer = false
                }
            )
        } else {
            HomeScreen(
                onOpenDrawer = {
                    showDrawer = true
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DARKCALC",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Personal Launcher",
            fontSize = 16.sp
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Button(
            onClick = onOpenDrawer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("APP DRAWER")
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedButton(
            onClick = {
                // Calculator will be added later.
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CALCULATOR")
        }
    }
}

@Composable
fun AppDrawer(
    apps: List<InstalledApp>,
    onLaunchApp: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "APP DRAWER",
                fontSize = 24.sp
            )

            OutlinedButton(
                onClick = onBack
            ) {
                Text("BACK")
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 24.dp
            )
        ) {
            items(
                items = apps,
                key = {
                    it.packageName
                }
            ) { app ->

                Text(
                    text = app.label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onLaunchApp(app.packageName)
                        }
                        .padding(vertical = 12.dp),
                    fontSize = 18.sp
                )
            }
        }
    }
}