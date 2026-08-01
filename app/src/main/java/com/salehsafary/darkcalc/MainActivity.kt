package com.salehsafary.darkcalc

import android.content.Context
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salehsafary.darkcalc.ui.theme.DarkCalcTheme
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

data class InstalledApp(
    val label: String,
    val packageName: String,
    val icon: Bitmap,
    val isCalculator: Boolean = false
)

enum class LauncherScreen {
    HOME,
    APP_DRAWER,
    CALCULATOR,
    LOGIN,
    RESET_PASSWORD,
    HIDDEN_APPS
}

private const val AUTH_PREFS = "darkcalc_auth"
private const val PASSWORD_HASH = "password_hash"

private const val ADMIN_USERNAME = "admin"
private const val DEFAULT_PASSWORD = "123456789"

private const val SECRET_CODE = "2580"

private const val SECURITY_QUESTION =
    "معلم محبوبت کیه؟"

private const val SECURITY_ANSWER =
    "آقای ساعدی"

private const val TELEGRAM_PACKAGE =
    "org.telegram.messenger"


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DarkCalcTheme {
                DarkCalcLauncher(
                    activity = this
                )
            }
        }
    }

    fun getInstalledApps(): List<InstalledApp> {

        val pm = packageManager

        val hiddenPackages = setOf(
            TELEGRAM_PACKAGE
        )

        val installedApplications =
            pm.getInstalledApplications(
                PackageManager.GET_META_DATA
            )

        val result =
            mutableListOf<InstalledApp>()

        for (appInfo in installedApplications) {

            val launchIntent =
                pm.getLaunchIntentForPackage(
                    appInfo.packageName
                )

            if (launchIntent == null) {
                continue
            }

            if (appInfo.packageName == packageName) {
                continue
            }

            if (appInfo.packageName in hiddenPackages) {
                continue
            }

            val label =
                pm.getApplicationLabel(
                    appInfo
                ).toString()

            val icon =
                drawableToBitmap(
                    appInfo.loadIcon(pm)
                )

            result.add(
                InstalledApp(
                    label = label,
                    packageName = appInfo.packageName,
                    icon = icon
                )
            )
        }

        return result
            .distinctBy { app -> app.packageName }
            .sortedBy { app -> app.label }
    }

    fun getHiddenApps(): List<InstalledApp> {

        val pm = packageManager
        val result = mutableListOf<InstalledApp>()

        try {

            val appInfo =
                pm.getApplicationInfo(
                    TELEGRAM_PACKAGE,
                    PackageManager.GET_META_DATA
                )

            val launchIntent =
                pm.getLaunchIntentForPackage(
                    TELEGRAM_PACKAGE
                )

            if (launchIntent != null) {

                result.add(
                    InstalledApp(
                        label =
                            pm.getApplicationLabel(
                                appInfo
                            ).toString(),

                        packageName =
                            TELEGRAM_PACKAGE,

                        icon =
                            drawableToBitmap(
                                appInfo.loadIcon(pm)
                            )
                    )
                )
            }

        } catch (
            _: PackageManager.NameNotFoundException
        ) {
        }

        return result
    }

    fun launchApp(
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

    fun drawableToBitmap(
        drawable: Drawable
    ): Bitmap {

        val width =
            if (drawable.intrinsicWidth > 0)
                drawable.intrinsicWidth
            else
                96

        val height =
            if (drawable.intrinsicHeight > 0)
                drawable.intrinsicHeight
            else
                96

        val bitmap =
            Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )

        val canvas =
            Canvas(bitmap)

        drawable.setBounds(
            0,
            0,
            canvas.width,
            canvas.height
        )

        drawable.draw(canvas)

        return bitmap
    }

    fun checkLogin(
        username: String,
        password: String
    ): Boolean {

        if (username != ADMIN_USERNAME) {
            return false
        }

        val prefs =
            getSharedPreferences(
                AUTH_PREFS,
                Context.MODE_PRIVATE
            )

        val savedHash =
            prefs.getString(
                PASSWORD_HASH,
                null
            )

        if (savedHash == null) {
            return password == DEFAULT_PASSWORD
        }

        return sha256(password) == savedHash
    }

    fun changePassword(
        newPassword: String
    ) {

        getSharedPreferences(
            AUTH_PREFS,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                PASSWORD_HASH,
                sha256(newPassword)
            )
            .apply()
    }

    fun checkSecurityAnswer(
        answer: String
    ): Boolean {

        return normalizeText(answer) ==
                normalizeText(SECURITY_ANSWER)
    }

    private fun normalizeText(
        text: String
    ): String {

        return text
            .trim()
            .replace("ي", "ی")
            .replace("ك", "ک")
            .replace("‌", "")
            .lowercase(Locale("fa"))
    }

    private fun sha256(
        value: String
    ): String {

        val digest =
            MessageDigest
                .getInstance("SHA-256")
                .digest(
                    value.toByteArray(
                        Charsets.UTF_8
                    )
                )

        return digest.joinToString("") {
            "%02x".format(it)
        }
    }
}


/* =========================
   LAUNCHER
   ========================= */

@Composable
fun DarkCalcLauncher(
    activity: MainActivity
) {

    val apps =
        remember {
            activity.getInstalledApps()
        }

    var screen by remember {
        mutableStateOf(
            LauncherScreen.HOME
        )
    }

    val calculator =
        remember {

            InstalledApp(
                label = "ماشین حساب",
                packageName = "darkcalc.calculator",
                icon =
                    activity.drawableToBitmap(
                        activity.applicationInfo
                            .loadIcon(
                                activity.packageManager
                            )
                    ),
                isCalculator = true
            )
        }

    Surface(
        modifier =
            Modifier.fillMaxSize(),

        color =
            MaterialTheme
                .colorScheme
                .background
    ) {

        when (screen) {

            LauncherScreen.HOME -> {

                HomeScreen(
                    apps = apps,
                    calculator = calculator,

                    onLaunch = {
                        activity.launchApp(
                            it
                        )
                    },

                    onCalculator = {
                        screen =
                            LauncherScreen.CALCULATOR
                    },

                    onDrawer = {
                        screen =
                            LauncherScreen.APP_DRAWER
                    }
                )
            }

            LauncherScreen.APP_DRAWER -> {

                AppDrawer(
                    apps = apps,
                    calculator = calculator,

                    onLaunch = {
                        activity.launchApp(
                            it
                        )
                    },

                    onCalculator = {
                        screen =
                            LauncherScreen.CALCULATOR
                    },

                    onBack = {
                        screen =
                            LauncherScreen.HOME
                    }
                )
            }

            LauncherScreen.CALCULATOR -> {

                CalculatorScreen(

                    onBack = {
                        screen =
                            LauncherScreen.HOME
                    },

                    onSecretCode = {
                        screen =
                            LauncherScreen.LOGIN
                    }
                )
            }

            LauncherScreen.LOGIN -> {

                LoginScreen(

                    activity = activity,

                    onBack = {
                        screen =
                            LauncherScreen.CALCULATOR
                    },

                    onSuccess = {
                        screen =
                            LauncherScreen.HIDDEN_APPS
                    },

                    onReset = {
                        screen =
                            LauncherScreen.RESET_PASSWORD
                    }
                )
            }

            LauncherScreen.RESET_PASSWORD -> {

                ResetPasswordScreen(

                    activity = activity,

                    onBack = {
                        screen =
                            LauncherScreen.LOGIN
                    },

                    onDone = {
                        screen =
                            LauncherScreen.LOGIN
                    }
                )
            }

            LauncherScreen.HIDDEN_APPS -> {

                val hiddenApps =
                    remember {
                        activity.getHiddenApps()
                    }

                HiddenAppsScreen(

                    apps =
                        hiddenApps,

                    onLaunch = {
                        activity.launchApp(
                            it
                        )
                    },

                    onBack = {
                        screen =
                            LauncherScreen.HOME
                    }
                )
            }
        }
    }
}


/* =========================
   HOME
   ========================= */

@Composable
fun HomeScreen(
    apps: List<InstalledApp>,
    calculator: InstalledApp,
    onLaunch: (String) -> Unit,
    onCalculator: () -> Unit,
    onDrawer: () -> Unit
) {

    val allApps =
        listOf(calculator) + apps

    val pages =
        if (allApps.isEmpty()) {
            listOf(emptyList())
        } else {
            allApps.chunked(20)
        }

    var page by remember {
        mutableStateOf(0)
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(
                        pages.size
                    ) {

                        var drag = 0f

                        detectHorizontalDragGestures(

                            onHorizontalDrag = {
                                change,
                                amount ->

                                change.consume()

                                drag += amount
                            },

                            onDragEnd = {

                                if (
                                    drag < -100 &&
                                    page < pages.lastIndex
                                ) {
                                    page++
                                }

                                if (
                                    drag > 100 &&
                                    page > 0
                                ) {
                                    page--
                                }

                                drag = 0f
                            }
                        )
                    }
        ) {

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp)
            ) {

                LiveClock()

                LiveDate()

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                LazyVerticalGrid(
                    columns =
                        GridCells.Fixed(4),

                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(20.dp)
                ) {

                    items(
                        pages[page],
                        key = {
                            it.packageName
                        }
                    ) { app ->

                        AppIcon(
                            app = app
                        ) {

                            if (
                                app.isCalculator
                            ) {
                                onCalculator()
                            } else {
                                onLaunch(
                                    app.packageName
                                )
                            }
                        }
                    }
                }
            }
        }

        PageIndicator(
            pageCount =
                pages.size,

            currentPage =
                page
        )

        Dock(
            onDrawer =
                onDrawer,

            onCalculator =
                onCalculator
        )
    }
}


/* =========================
   PAGE INDICATOR
   ========================= */

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int
) {

    if (pageCount <= 1) {
        return
    }

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.Center
    ) {

        repeat(pageCount) { index ->

            Text(
                text =
                    if (
                        index == currentPage
                    ) {
                        "●"
                    } else {
                        "○"
                    },

                fontSize =
                    12.sp,

                modifier =
                    Modifier.padding(
                        horizontal = 3.dp
                    )
            )
        }
    }

    Spacer(
        modifier =
            Modifier.height(5.dp)
    )
}


/* =========================
   CLOCK
   ========================= */

@Composable
fun LiveClock() {

    var time by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        while (true) {

            time =
                SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(Date())

            delay(1000)
        }
    }

    Text(
        text =
            time,

        fontSize =
            48.sp,

        fontWeight =
            FontWeight.Light
    )
}


/* =========================
   DATE
   ========================= */

@Composable
fun LiveDate() {

    var date by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        while (true) {

            date =
                SimpleDateFormat(
                    "EEEE، d MMMM yyyy",
                    Locale("fa")
                ).format(Date())

            delay(60000)
        }
    }

    Text(
        text =
            date,

        fontSize =
            14.sp
    )
}


/* =========================
   APP ICON
   ========================= */

@Composable
fun AppIcon(
    app: InstalledApp,
    onClick: () -> Unit
) {

    Column(
        modifier =
            Modifier
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
            text =
                app.label,

            fontSize =
                11.sp,

            maxLines =
                1
        )
    }
}


/* =========================
   DOCK
   ========================= */

@Composable
fun Dock(
    onDrawer: () -> Unit,
    onCalculator: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme
                        .colorScheme
                        .surface
                )
                .padding(
                    vertical = 10.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceEvenly
    ) {

        DockButton(
            text = "▦",
            label = "برنامه‌ها",
            onClick = onDrawer
        )

        DockButton(
            text = "🧮",
            label = "ماشین حساب",
            onClick = onCalculator
        )
    }
}


@Com