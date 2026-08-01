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

    /**
     * دریافت برنامه‌های نصب‌شده‌ای که Intent قابل اجرا دارند.
     *
     * شامل:
     * - برنامه‌های معمولی
     * - برنامه‌های سیستمی قابل اجرا
     * - نام واقعی برنامه
     * - آیکن واقعی برنامه
     */
    private fun getInstalledApps(): List<InstalledApp> {

        val pm = packageManager

        return pm.getInstalledApplications(
            PackageManager.GET_META_DATA
        )
            .asSequence()

            // فقط برنامه‌هایی که قابل اجرا هستند
            .filter { appInfo ->
                pm.getLaunchIntentForPackage(
                    appInfo.packageName
                ) != null
            }

            // خود DarkCalc داخل App Drawer نمایش داده نشود
            .filter { appInfo ->
                appInfo.packageName != packageName
            }

            // تبدیل ApplicationInfo به InstalledApp
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

            // جلوگیری از تکراری شدن Package
            .distinctBy {
                it.packageName
            }

            // مرتب‌سازی الفبایی
            .sortedBy {
                it.label.lowercase()
            }

            .toList()
    }

    /**
     * تبدیل Drawable آیکن برنامه به Bitmap
     * تا Compose بتواند آن را نمایش دهد.
     */
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

    /**
     * اجرای برنامه انتخاب‌شده
     */
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

/* ------------------------------------------------ */
/* Launcher                                       */
/* ------------------------------------------------ */

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

/* ------------------------------------------------ */
/* Home Screen                                    */
/* ------------------------------------------------ */

@Composable
fun HomeScreen(
    apps: List<InstalledApp>,
    onLaunchApp: (String) -> Unit,
    onOpenDrawer: () -> Unit
) {

    /*
     * چند برنامه اول برای صفحه اصلی.
     * تمام برنامه‌ها همچنان در App Drawer هستند.
     */
    val homeApps =
        apps.take(8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 32.dp,
                bottom = 12.dp
            )
    ) {

        /* Header */

        Text(
            text = "DARKCALC",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Launcher",
            fontSize = 13.sp
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        /* Home App Grid */

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),

            modifier = Modifier.weight(1f),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp),

            verticalArrangement =
                Arrangement.spacedBy(20.dp),

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

/* ------------------------------------------------ */
/* App Icon                                       */
/* ------------------------------------------------ */

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

/* ------------------------------------------------ */
/* Dock                                           */
/* ------------------------------------------------ */

@Composable
fun Dock(
    onOpenDrawer: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface
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
            onClick = onOpenDrawer
        )

        DockButton(
            symbol = "⌕",
            label = "جستجو",
            onClick = {
                // Search will be added later.
            }
        )

        DockButton(
            symbol = "⚙",
            label = "تنظیمات",
            onClick = {
                // Launcher settings will be added later.
            }
        )
    }
}

/* ------------------------------------------------ */
/* Dock Button                                    */
/* ------------------------------------------------ */

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

/* ------------------------------------------------ */
/* App Drawer                                     */
/* ------------------------------------------------ */

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

        /* Drawer Header */

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

        /* Complete App List */

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
                            app.icon.asImageBitmap(),

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