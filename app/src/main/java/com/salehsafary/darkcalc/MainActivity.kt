package com.salehsafary.darkcalc

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
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

private const val CALCULATOR_PACKAGE =
    "darkcalc.calculator"


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

    /*
     * تمام برنامه‌های قابل اجرا
     * به‌جز خود لانچر و Telegram
     */
    private fun getInstalledApps(): List<InstalledApp> {

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

            if (hiddenPackages.contains(
                    appInfo.packageName
                )
            ) {
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

        val uniqueApps =
            result.distinctBy {
                app -> app.packageName
            }

        return uniqueApps.sortedBy {
            app -> app.label
        }
    }

    /*
     * برنامه‌های مخفی
     */
    private fun getHiddenApps(): List<InstalledApp> {

        val pm = packageManager

        val result =
            mutableListOf<InstalledApp>()

        val hiddenPackages =
            listOf(
                TELEGRAM_PACKAGE
            )

        for (pkg in hiddenPackages) {

            try {

                val appInfo =
                    pm.getApplicationInfo(
                        pkg,
                        PackageManager.GET_META_DATA
                    )

                val launchIntent =
                    pm.getLaunchIntentForPackage(
                        pkg
                    )

                if (launchIntent == null) {
                    continue
                }

                result.add(
                    InstalledApp(
                        label =
                            pm.getApplicationLabel(
                                appInfo
                            ).toString(),

                        packageName = pkg,

                        icon =
                            drawableToBitmap(
                                appInfo.loadIcon(pm)
                            )
                    )
                )

            } catch (
                _: PackageManager.NameNotFoundException
            ) {
                // برنامه نصب نیست
            }
        }

        return result
    }

    /*
     * تبدیل Drawable به Bitmap
     */
    private fun drawableToBitmap(
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

    /*
     * اجرای برنامه
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

    /*
     * بررسی ورود
     */
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

        val passwordHash =
            sha256(password)

        return if (savedHash == null) {

            password == DEFAULT_PASSWORD

        } else {

            passwordHash == savedHash
        }
    }

    /*
     * تغییر رمز
     */
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

    /*
     * بررسی سؤال امنیتی
     */
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

    /*
     * SHA-256 برای ذخیره رمز
     */
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

        return Base64.encodeToString(
            digest,
            Base64.NO_WRAP
        )
    }
}


/* ================================================= */
/* MAIN LAUNCHER                                    */
/* ================================================= */

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

    val context =
        androidx.compose.ui.platform
            .LocalContext
            .current

    val activity =
        context as MainActivity

    val calculatorIcon =
        remember {

            activity.drawableToBitmapForCalculator(
                activity.applicationInfo
                    .loadIcon(activity.packageManager)
            )
        }

    val calculator =
        remember {

            InstalledApp(
                label = "ماشین حساب",
                packageName = CALCULATOR_PACKAGE,
                icon = calculatorIcon,
                isCalculator = true
            )
        }

    val homeApps =
        remember(apps) {

            listOf(
                calculator
            ) + apps
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
                    apps = homeApps,

                    onLaunchApp =
                        onLaunchApp,

                    onOpenDrawer = {
                        screen =
                            LauncherScreen.APP_DRAWER
                    },

                    onOpenCalculator = {
                        screen =
                            LauncherScreen.CALCULATOR
                    }
                )
            }

            LauncherScreen.APP_DRAWER -> {

                AppDrawer(
                    apps = apps,

                    calculator = calculator,

                    onLaunchApp =
                        onLaunchApp,

                    onOpenCalculator = {
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

                    onLaunchApp =
                        onLaunchApp,

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
/* BITMAP HELPER                                    */
/* ================================================= */

private fun MainActivity
    .drawableToBitmapForCalculator(
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


/* ================================================= */
/* HOME SCREEN                                      */
/* ================================================= */

@Composable
fun HomeScreen(
    apps: List<InstalledApp>,
    onLaunchApp: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenCalculator: () -> Unit
) {

    val appsPerPage = 20

    val pages =
        if (apps.isEmpty()) {

            listOf(emptyList())

        } else {

            apps.chunked(
                appsPerPage
            )
        }

    var currentPage by remember {
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

                        var totalDrag = 0f

                        detectHorizontalDragGestures(

                            onDragStart = {
                                totalDrag = 0f
                            },

                            onHorizontalDrag = {
                                change,
                                dragAmount ->

                                change.consume()

                                totalDrag +=
                                    dragAmount
                            },

                            onDragEnd = {

                                if (
                                    totalDrag < -120f &&
                                    currentPage <
                                    pages.lastIndex
                                ) {

                                    currentPage++
                                }

                                if (
                                    totalDrag > 120f &&
                                    currentPage > 0
                                ) {

                                    currentPage--
                                }

                                totalDrag = 0f
                            },

                            onDragCancel = {
                                totalDrag = 0f
                            }
                        )
                    }
        ) {

            HomePage(
                apps =
                    pages[currentPage],

                onLaunchApp =
                    onLaunchApp,

                onOpenCalculator =
                    onOpenCalculator
            )
        }

        PageIndicator(
            pageCount =
                pages.size,

            currentPage =
                currentPage
        )

        Dock(
            onOpenDrawer =
                onOpenDrawer,

            onOpenCalculator =
                onOpenCalculator
        )
    }
}


/* ================================================= */
/* HOME PAGE                                        */
/* ================================================= */

@Composable
fun HomePage(
    apps: List<InstalledApp>,
    onLaunchApp: (String) -> Unit,
    onOpenCalculator: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 28.dp
                )
    ) {

        LiveClock()

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        LiveDate()

        Spacer(
            modifier =
                Modifier.height(30.dp)
        )

        LazyVerticalGrid(
            columns =
                GridCells.Fixed(4),

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
                items = apps,

                key = {
                    it.packageName
                }
            ) { app ->

                AppIcon(
                    app = app
                ) {

                    if (app.isCalculator) {

                        onOpenCalculator()

                    } else {

                        onLaunchApp(
                            app.packageName
                        )
                    }
                }
            }
        }
    }
}


/* ================================================= */
/* PAGE INDICATOR                                   */
/* ================================================= */

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
                        index ==
                        currentPage
                    ) {
                        "●"
                    } else {
                        "○"
                    },

                fontSize =
 