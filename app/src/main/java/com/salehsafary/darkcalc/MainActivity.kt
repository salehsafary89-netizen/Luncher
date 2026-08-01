package com.salehsafary.darkcalc

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    DRAWER,
    CALCULATOR,
    LOGIN,
    RESET,
    HIDDEN
}


private const val PREFS_NAME = "darkcalc_auth"
private const val PASSWORD_KEY = "password_hash"

private const val USERNAME = "admin"
private const val DEFAULT_PASSWORD = "123456789"

private const val SECRET_CODE = "2580"

private const val SECURITY_QUESTION =
    "معلم محبوبت کیه؟"

private const val SECURITY_ANSWER =
    "آقای ساعدی"

private const val TELEGRAM_PACKAGE =
    "org.telegram.messenger"


class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {
            DarkCalcTheme {
                DarkCalcApp(
                    activity = this
                )
            }
        }
    }


    fun getInstalledApps(): List<InstalledApp> {

        val pm = packageManager

        return pm
            .getInstalledApplications(
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
            .filter { appInfo ->

                appInfo.packageName !=
                    TELEGRAM_PACKAGE
            }
            .map { appInfo ->

                InstalledApp(
                    label =
                        pm.getApplicationLabel(
                            appInfo
                        ).toString(),

                    packageName =
                        appInfo.packageName,

                    icon =
                        drawableToBitmap(
                            appInfo.loadIcon(pm)
                        )
                )
            }
            .distinctBy { app ->
                app.packageName
            }
            .sortedBy { app ->
                app.label.lowercase(
                    Locale.getDefault()
                )
            }
            .toList()
    }


    fun getHiddenApps(): List<InstalledApp> {

        val pm = packageManager
        val result =
            mutableListOf<InstalledApp>()

        try {

            val info =
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
                                info
                            ).toString(),

                        packageName =
                            TELEGRAM_PACKAGE,

                        icon =
                            drawableToBitmap(
                                info.loadIcon(pm)
                            )
                    )
                )
            }

        } catch (
            _: PackageManager.NameNotFoundException
        ) {
            // Telegram نصب نیست.
        }

        return result
    }


    fun launchPackage(
        packageName: String
    ) {

        val intent =
            packageManager
                .getLaunchIntentForPackage(
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

        if (username != USERNAME) {
            return false
        }

        val prefs =
            getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val savedHash =
            prefs.getString(
                PASSWORD_KEY,
                null
            )

        if (savedHash == null) {
            return password ==
                DEFAULT_PASSWORD
        }

        return sha256(password) ==
            savedHash
    }


    fun changePassword(
        newPassword: String
    ) {

        getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                PASSWORD_KEY,
                sha256(newPassword)
            )
            .apply()
    }


    fun checkSecurityAnswer(
        answer: String
    ): Boolean {

        return normalizeText(answer) ==
            normalizeText(
                SECURITY_ANSWER
            )
    }


    private fun normalizeText(
        value: String
    ): String {

        return value
            .trim()
            .replace("ي", "ی")
            .replace("ك", "ک")
            .replace("‌", "")
            .lowercase(
                Locale("fa")
            )
    }


    private fun sha256(
        value: String
    ): String {

        val bytes =
            MessageDigest
                .getInstance("SHA-256")
                .digest(
                    value.toByteArray(
                        Charsets.UTF_8
                    )
                )

        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }
}


/* =========================================================
   ROOT
   ========================================================= */

@Composable
fun DarkCalcApp(
    activity: MainActivity
) {

    var screen by remember {
        mutableStateOf(
            LauncherScreen.HOME
        )
    }

    val apps = remember {
        activity.getInstalledApps()
    }

    val calculator = remember {

        InstalledApp(
            label = "ماشین حساب",

            packageName =
                "darkcalc.calculator",

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
                        activity.launchPackage(it)
                    },

                    onCalculator = {
                        screen =
                            LauncherScreen.CALCULATOR
                    },

                    onDrawer = {
                        screen =
                            LauncherScreen.DRAWER
                    }
                )
            }


            LauncherScreen.DRAWER -> {

                AppDrawer(
                    apps = apps,
                    calculator = calculator,

                    onLaunch = {
                        activity.launchPackage(it)
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
                            LauncherScreen.HIDDEN
                    },

                    onReset = {
                        screen =
                            LauncherScreen.RESET
                    }
                )
            }


            LauncherScreen.RESET -> {

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


            LauncherScreen.HIDDEN -> {

                val hiddenApps =
                    remember {
                        activity.getHiddenApps()
                    }

                HiddenAppsScreen(

                    apps = hiddenApps,

                    onLaunch = {
                        activity.launchPackage(it)
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


/* =========================================================
   HOME SCREEN
   ========================================================= */

@OptIn(ExperimentalAnimationApi::class)
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

    var currentPage by remember {
        mutableIntStateOf(0)
    }

    val safePage =
        currentPage.coerceIn(
            0,
            pages.lastIndex
        )


    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(
                        pages.size
                    ) {

                        var dragAmount = 0f

                        detectHorizontalDragGestures(

                            onHorizontalDrag = {
                                change,
                                amount ->

                                change.consume()

                                dragAmount +=
                                    amount
                            },

                            onDragEnd = {

                                if (
                                    dragAmount < -80f &&
                                    safePage <
                                    pages.lastIndex
                                ) {

                                    currentPage =
                                        safePage + 1
                                }

                                if (
                                    dragAmount > 80f &&
                                    safePage > 0
                                ) {

                                    currentPage =
                                        safePage - 1
                                }

                                dragAmount = 0f
                            }
                        )
                    }
        ) {

            AnimatedContent(
                targetState = safePage,

                transitionSpec = {

                    if (
                        targetState >
                        initialState
                    ) {

                        (
                            slideInHorizontally(
                                initialOffsetX = {
                                    it
                                },
                                animationSpec =
                                    tween(
                                        280,
                                        easing =
                                            FastOutSlowInEasing
                                    )
                            ) +
                                fadeIn(
                                    tween(180)
                                )
                        ) togetherWith
                        (
                            slideOutHorizontally(
                                targetOffsetX = {
                                    -it
                                },
                                animationSpec =
                                    tween(
                                        280,
                                        easing =
                                            FastOutSlowInEasing
                                    )
                            ) +
                                fadeOut(
                                    tween(180)
                                )
                        )

                    } else {

                        (
                            slideInHorizontally(
                                initialOffsetX = {
                                    -it
                                },
                                animationSpec =
                                    tween(
                                        280,
                                        easing =
                                            FastOutSlowInEasing
                                    )
                            ) +
                                fadeIn(
                                    tween(180)
                                )
                        ) togetherWith
                        (
                            slideOutHorizontally(
                                targetOffsetX = {
                                    it
                                },
                                animationSpec =
                                    tween(
                                        280,
                                        easing =
                                            FastOutSlowInEasing
                                    )
                            ) +
                                fadeOut(
                                    tween(180)
                                )
                        )
                    }
                },

                label =
                    "HomePageAnimation"
            ) { page ->

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            )
                ) {

                    LiveClock()

                    LiveDate()

                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )

                    LazyVerticalGrid(
                        columns =
                            GridCells.Fixed(4),

                        modifier =
                            Modifier.fillMaxSize(),

                        contentPadding =
                            PaddingValues(
                                bottom = 20.dp
                            ),

                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(20.dp)
                    ) {
items(
                    apps,
                    key = { app ->
                        app.packageName
                    }
                ) { app ->

                    AppIcon(
                        app = app,
                        onClick = {
                            onLaunch(app.packageName)
                        }
                    )
                }
            )
        }
    }
}

        