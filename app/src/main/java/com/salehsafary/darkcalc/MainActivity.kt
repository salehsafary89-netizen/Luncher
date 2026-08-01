package com.salehsafary.darkcalc

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salehsafary.darkcalc.ui.theme.DarkCalcTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InstalledApp(
    val label: String,
    val packageName: String,
    val icon: Bitmap
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DarkCalcTheme {

                val apps = remember {
                    getInstalledApps()
                }

                DarkCalcLauncher(
                    apps = apps,
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
            .filter { appInfo ->
                pm.getLaunchIntentForPackage(
                    appInfo.packageName
                ) != null
            }
            .filter { appInfo ->
                appInfo.packageName != packageName
            }
            .map { appInfo ->

                InstalledApp(
                    label = pm
                        .getApplicationLabel(appInfo)
                        .toString(),

                    packageName = appInfo.packageName,

                    icon = drawableToBitmap(
                        appInfo.loadIcon(pm)
                    )
                )
            }
            .distinctBy {
                it.packageName
            }
            .sortedBy {
                it.label.lowercase()
            }
            .toList()
    }

    private fun drawableToBitmap(
        drawable: Drawable
    ): Bitmap {

        val width =
            if (drawable.intrinsicWidth > 0) {
                drawable.intrinsicWidth
            } else {
                96
            }

        val height =
            if (drawable.intrinsicHeight > 0) {
                drawable.intrinsicHeight
            } else {
                96
            }

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        drawable.setBounds(
            0,
            0,
            canvas.width,
            canvas.height
        )

        drawable.draw(canvas)

        return bitmap
    }

    private fun launchApp(
        packageName: String
    ) {

        val intent =
            packageManager.getLaunchIntentForPackage(
                packageName
            )

        if (intent != null) {
            startActivity(intent)
        }
    }
}

enum class LauncherScreen {
    HOME,
    APP_DRAWER
}

@Composable
fun DarkCalcLauncher(
    apps: List<InstalledApp>,
    onLaunchApp: (String) -> Unit
) {

    var screen by remember {
        mutableStateOf(
            LauncherScreen.HOME
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        when (screen) {

            LauncherScreen.HOME -> {

                HomeScreen(
                    apps = apps,
                    onLaunchApp = onLaunchApp,
                    onOpenDrawer = {
                        screen =
                            LauncherScreen.APP_DRAWER
                    }
                )
            }

            LauncherScreen.APP_DRAWER -> {

                AppDrawer(
                    apps = apps,
                    onLaunchApp = onLaunchApp,
                    onBack = {
                        screen =
                            LauncherScreen.HOME
                    }
                )
            }
        }
    }
}

/* ================================================= */
/* HOME SCREEN                                      */
/* ================================================= */

@Composable
fun HomeScreen(
    apps: List<InstalledApp>,
    onLaunchApp: (String) -> Unit,
    onOpenDrawer: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 28.dp,
                bottom = 12.dp
            )
    ) {

        /* ساعت */

        LiveClock()

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        /* تاریخ */

        LiveDate()

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        /*
         * برنامه‌های صفحه اصلی
         */
        val homeApps =
            apps.take(8)

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),

            modifier =
                Modifier.weight(1f),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp),

            verticalArrangement =
                Arrangement.spacedBy(22.dp),

            contentPadding =
                PaddingValues(
                    bottom = 20.dp
                )
        ) {

            items(
                items = homeApps,

                key = {
                    it.packageName
                }
            ) { app ->

                AppIcon(
                    app = app,

                    onClick = {
                        onLaunchApp(
                            app.packageName
                        )
                    }
                )
            }
        }

        /* Dock */

        Dock(
            onOpenDrawer = onOpenDrawer
        )
    }
}

/* ================================================= */
/* LIVE CLOCK                                       */
/* ================================================= */

@Composable
fun LiveClock() {

    var currentTime by remember {
        mutableStateOf(
            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date())
        )
    }

    LaunchedEffect(Unit) {

        while (true) {

            currentTime =
                SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(Date())

            delay(1000)
        }
    }

    Text(
        text = currentTime,

        fontSize = 52.sp,

        fontWeight =
            FontWeight.Light,

        color =
            MaterialTheme
                .colorScheme
                .primary
    )
}

/* ================================================= */
/* LIVE DATE                                        */
/* ================================================= */

@Composable
fun LiveDate() {

    var currentDate by remember {

        mutableStateOf(
            SimpleDateFormat(
                "EEEE، d MMMM yyyy",
                Locale("fa")
            ).format(Date())
        )
    }

    LaunchedEffect(Unit) {

        while (true) {

            currentDate =
                SimpleDateFormat(
                    "EEEE، d MMMM yyyy",
                    Locale("fa")
                ).format(Date())

            delay(60000)
        }
    }

    Text(
        text = currentDate,

        fontSize = 15.sp,

        color =
            MaterialTheme
                .colorScheme
                .onBackground
    )
}

/* ================================================= */
/* APP ICON                                         */
/* ================================================= */

@Composable
fun AppIcon(
    app: InstalledApp,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Image(
            bitmap =
                app.icon.asImageBitmap(),

            contentDescription =
                app.label,

            modifier =
                Modifier.size(56.dp)
        )

        Spacer(
            modifier =
                Modifier.height(5.dp)
        )

        Text(
            text = app.label,

            fontSize = 12.sp,

            maxLines = 1
        )
    }
}

/* ================================================= */
/* DOCK                                             */
/* ================================================= */

@Composable
fun Dock(
    onOpenDrawer: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme
                    .colorScheme
                    .surface
            )
            .padding(
                vertical = 10.dp,
                horizontal = 12.dp
            ),

        horizontalArrangement =
            Arrangement.SpaceEvenly,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        DockButton(
            symbol = "▦",

            label = "برنامه‌ها",

            onClick =
                onOpenDrawer
        )

        DockButton(
            symbol = "⌕",

            label = "جستجو",

            onClick = {
                // در مرحله بعد اضافه می‌شود
            }
        )

        DockButton(
            symbol = "⚙",

            label = "تنظیمات",

            onClick = {
                // در مرحله بعد اضافه می‌شود
            }
        )
    }
}

/* ================================================= */
/* DOCK BUTTON                                      */
/* ================================================= */

@Composable
fun DockButton(
    symbol: String,
    label: String,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 18.dp,
                vertical = 4.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = symbol,

            fontSize = 25.sp,

            color =
                MaterialTheme
                    .colorScheme
                    .primary
        )

        Text(
            text = label,

            fontSize = 10.sp
        )
    }
}

/* ================================================= */
/* APP DRAWER                                       */
/* ================================================= */

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
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column {

                Text(
                    text = "برنامه‌ها",

                    fontSize = 26.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "${apps.size} برنامه",

                    fontSize = 13.sp
                )
            }

            OutlinedButton(
                onClick = onBack
            ) {

                Text(
                    text = "خانه"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        LazyColumn(
            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    bottom = 24.dp
                )
        ) {

            items(
                items = apps,

                key = {
                    it.packageName
                }
            ) { app ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {

                            onLaunchApp(
                                app.packageName
                            )
                        }
                        .padding(
                            vertical = 10.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Image(
                        bitmap =
                            app.icon
                                .asImageBitmap(),

                        contentDescription =
                            app.label,

                        modifier =
                            Modifier.size(48.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.size(14.dp)
                    )

                    Text(
                        text = app.label,

                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}