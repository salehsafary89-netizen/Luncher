package com.salehsafary.darkcalc

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
                DarkCalcApp()
            }
        }
    }

    fun getInstalledApps(): List<InstalledApp> {
        val packageManager = packageManager

        return packageManager
            .getInstalledApplications(PackageManager.GET_META_DATA)
            .mapNotNull { appInfo ->
                val launchIntent =
                    packageManager.getLaunchIntentForPackage(appInfo.packageName)
                        ?: return@mapNotNull null

                val label = packageManager
                    .getApplicationLabel(appInfo)
                    .toString()

                InstalledApp(
                    label = label,
                    packageName = appInfo.packageName
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}

@Composable
fun DarkCalcApp() {

    var showDrawer by remember {
        mutableStateOf(false)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        if (showDrawer) {
            AppDrawer(
                apps = remember {
                    emptyList<InstalledApp>()
                },
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
                // Calculator will be added in the next phase.
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
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            items(apps) { app ->

                Text(
                    text = app.label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    fontSize = 18.sp
                )
            }
        }
    }
}