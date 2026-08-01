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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DarkCalcTheme {
                DarkCalcApp(this)
            }
        }
    }

    fun getInstalledApps(): List<InstalledApp> {

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
            .filter { appInfo ->
                appInfo.packageName != TELEGRAM_PACKAGE
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
            .distinctBy { app ->
                app.packageName
            }
            .sortedBy { app ->
                app.label.lowercase(Locale.getDefault())
            }
            .toList()
    }

    fun getHiddenApps(): List<InstalledApp> {

        val pm = packageManager

        return try {

            val info = pm.getApplicationInfo(
                TELEGRAM_PACKAGE,
                PackageManager.GET_META_DATA
            )

            val launchIntent =
                pm.getLaunchIntentForPackage(
                    TELEGRAM_PACKAGE
                )

            if (launchIntent != null) {

                listOf(
                    InstalledApp(
                        label = pm
                            .getApplicationLabel(info)
                            .toString(),

                        packageName =
                            TELEGRAM_PACKAGE,

                        icon = drawableToBitmap(
                            info.loadIcon(pm)
                        )
                    )
                )

            } else {
                emptyList()
            }

        } catch (
            _: PackageManager.NameNotFoundException
        ) {
            emptyList()
        }
    }

    fun launchPackage(packageName: String) {

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

    fun checkLogin(
        username: String,
        password: String
    ): Boolean {

        if (username != USERNAME) {
            return false
        }

        val prefs = getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val savedHash = prefs.getString(
            PASSWORD_KEY,
            null
        )

        return if (savedHash == null) {
            password == DEFAULT_PASSWORD
        } else {
            sha256(password) == savedHash
        }
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
                normalizeText(SECURITY_ANSWER)
    }

    private fun normalizeText(
        value: String
    ): String {

        return value
            .trim()
            .replace("ي", "ی")
            .replace("ك", "ک")
            .replace("\u200C", "")
            .lowercase(Locale("fa"))
    }

    private fun sha256(
        value: String
    ): String {

        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(
                value.toByteArray(Charsets.UTF_8)
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
        mutableStateOf(LauncherScreen.HOME)
    }

    val apps = remember {
        activity.getInstalledApps()
    }

    val calculator = remember {

        InstalledApp(
            label = "ماشین حساب",
            packageName = "darkcalc.calculator",

            icon = activity.drawableToBitmap(
                activity.applicationInfo.loadIcon(
                    activity.packageManager
                )
            ),

            isCalculator = true
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

                val hiddenApps = remember {
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
   HOME
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

    val page =
        currentPage.coerceIn(
            0,
            pages.lastIndex
        )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(pages.size) {

                    var dragAmount = 0f

                    detectHorizontalDragGestures(

                        onHorizontalDrag = {
                                change,
                                amount ->

                            change.consume()

                            dragAmount += amount
                        },

                        onDragEnd = {

                            if (
                                dragAmount < -80f &&
                                page < pages.lastIndex
                            ) {
                                currentPage = page + 1
                            }

                            if (
                                dragAmount > 80f &&
                                page > 0
                            ) {
                                currentPage = page - 1
                            }

                            dragAmount = 0f
                        }
                    )
                }
        ) {

            AnimatedContent(
                targetState = page,

                transitionSpec = {

                    if (
                        targetState > initialState
                    ) {

                        (
                            slideInHorizontally(
                                initialOffsetX = { it },
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
                                targetOffsetX = { -it },
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
                                initialOffsetX = { -it },
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
                                targetOffsetX = { it },
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

                label = "HomePageAnimation"
            ) { selectedPage ->

                Column(
                    modifier = Modifier
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
                            items = pages[selectedPage],
                            key = { app ->
                                app.packageName
                            }
                        ) { app ->

                            AppIcon(
                                app = app,

                                onClick = {

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
                            )
                        }
                    }
                }
            }
        }

        PageIndicator(
            count = pages.size,
            current = page
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Dock(
            onDrawer = onDrawer
        )
    }
}


/* =========================================================
   PAGE INDICATOR
   ========================================================= */

@Composable
fun PageIndicator(
    count: Int,
    current: Int
) {

    if (count <= 1) {
        return
    }

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.Center
    ) {

        repeat(count) { index ->

            Text(Text(
                text = if (index == current) {
                    "●"
                } else {
                    "○"
                },
                fontSize = 11.sp,
                modifier = Modifier.padding(
                    horizontal = 3.dp
                )
            )
        }
    }
}


/* =========================================================
   APP ICON
   ========================================================= */

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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = app.icon.asImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier.size(58.dp)
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = app.label,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}


/* =========================================================
   APP DRAWER
   ========================================================= */

@Composable
fun AppDrawer(
    apps: List<InstalledApp>,
    calculator: InstalledApp,
    onLaunch: (String) -> Unit,
    onCalculator: () -> Unit,
    onBack: () -> Unit
) {
    val drawerApps = listOf(calculator) + apps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "برنامه‌ها",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = onBack
            ) {
                Text("خانه")
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            items(
                items = drawerApps,
                key = { app ->
                    app.packageName
                }
            ) { app ->

                AppIcon(
                    app = app,
                    onClick = {
                        if (app.isCalculator) {
                            onCalculator()
                        } else {
                            onLaunch(app.packageName)
                        }
                    }
                )
            }
        }
    }
}


/* =========================================================
   DOCK
   ========================================================= */

@Composable
fun Dock(
    onDrawer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface
            )
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {

        DockButton(
            text = "▦",
            label = "برنامه‌ها",
            onClick = onDrawer
        )
    }
}


/* =========================================================
   DOCK BUTTON
   ========================================================= */

@Composable
fun DockButton(
    text: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = text,
            fontSize = 24.sp
        )

        Text(
            text = label,
            fontSize = 10.sp
        )
    }
}


/* =========================================================
   LIVE CLOCK
   ========================================================= */

@Composable
fun LiveClock() {

    var time by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        while (true) {

            time = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date())

            delay(1000)
        }
    }

    Text(
        text = time,
        fontSize = 44.sp,
        fontWeight = FontWeight.Light
    )
}


/* =========================================================
   LIVE DATE
   ========================================================= */

@Composable
fun LiveDate() {

    var date by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        while (true) {

            date = SimpleDateFormat(
                "EEEE، d MMMM yyyy",
                Locale("fa")
            ).format(Date())

            delay(60000)
        }
    }

    Text(
        text = date,
        fontSize = 14.sp
    )
}


/* =========================================================
   CALCULATOR
   ========================================================= */

@Composable
fun CalculatorScreen(
    onBack: () -> Unit,
    onSecretCode: () -> Unit
) {

    var display by remember {
        mutableStateOf("")
    }

    fun press(value: String) {

        when (value) {

            "C" -> {
                display = ""
            }

            "=" -> {

                if (display == SECRET_CODE) {

                    display = ""
                    onSecretCode()

                } else {

                    display =
                        calculateSimple(display)
                }
            }

            else -> {
                display += value
            }
        }
    }

    val rows = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "−"),
        listOf("0", "C", "=", "+")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "ماشین حساب",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = onBack
            ) {
                Text("بازگشت")
            }
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Text(
            text = if (display.isEmpty()) {
                "0"
            } else {
                display
            },
            fontSize = 38.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        rows.forEach { row ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                row.forEach { value ->

                    Button(
                        onClick = {
                            press(value)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {

                        Text(
                            text = value,
                            fontSize = 21.sp
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }
    }
}


/* =========================================================
   CALCULATOR ENGINE
   ========================================================= */

fun calculateSimple(
    expression: String
): String {

    if (expression.isBlank()) {
        return "0"
    }

    return try {

        val normalized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")

        val operator =
            listOf('+', '-', '*', '/')
                .firstOrNull {
                    normalized.contains(it)
                }
                ?: return normalized

        val parts =
            normalized.split(operator)

        if (parts.size != 2) {
            return "خطا"
        }

        val first =
            parts[0].trim().toDouble()

        val second =
            parts[1].trim().toDouble()

        val result = when (operator) {

            '+' -> first + second

            '-' -> first - second

            '*' -> first * second

            '/' -> {
                if (second == 0.0) {
                    return "خطا"
                }
                first / second
            }

            else -> return "خطا"
        }

        if (!result.isFinite()) {
            "خطا"
        } else if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            result.toString()
        }

    } catch (_: Exception) {

        "خطا"
    }
}


/* =========================================================
   LOGIN
   ========================================================= */

@Composable
fun LoginScreen(
    activity: MainActivity,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onReset: () -> Unit
) {

    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 28.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "ورود",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    error = ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("نام کاربری")
                }
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    error = ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("رمز عبور")
                },
                visualTransformation =
                    PasswordVisualTransformation()
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            Button(
                onClick = {

                    if (
                        activity.checkLogin(
                            username.trim(),
                            password
                        )
                    ) {

                        error = ""
                        onSuccess()

                    } else {

                        error =
                            "نام کاربری یا رمز عبور اشتباه است"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape =
                    RoundedCornerShape(12.dp)
            ) {

                Text(
                    text = "ورود",
                    fontSize = 17.sp
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            TextButton(
                onClick = onReset
            ) {

                Text(
                    text = "فراموشی رمز عبور",
                    fontSize = 14.sp
                )
            }

            if (error.isNotEmpty()) {

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = error,
                    color =
                        MaterialTheme
                            .colorScheme
                            .error,
                    fontSize = 13.sp
                )
            }
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp)
        ) {

            Text("بازگشت")
        }
    }
}


/* =========================================================
   RESET PASSWORD
   ========================================================= */

@Composable
fun ResetPasswordScreen(
    activity: MainActivity,
    onBack: () -> Unit,
    onDone: () -> Unit
) {

    var answer by remember {
        mutableStateOf("")
    }

    var newPassword by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier = Modifier.height(55.dp)
        )

        Text(
            text = "بازنشانی رمز",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = SECURITY_QUESTION,
            fontSize = 17.sp
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = answer,
            onValueChange = {
                answer = it
                message = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("پاسخ")
            },
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                message = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("رمز جدید")
            },
            singleLine = true,
            visualTransformation =
                PasswordVisualTransformation()
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                message = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("تکرار رمز")
            },
            singleLine = true,
            visualTransformation =
                PasswordVisualTransformation()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = {

                when {

                    !activity.checkSecurityAnswer(
                        answer
                    ) -> {

                        message =
                            "پاسخ اشتباه است"
                    }

                    newPassword.length < 6 -> {

                        message =
                            "رمز باید حداقل ۶ کاراکتر باشد"
                    }

                    newPassword !=
                            confirmPassword -> {

                        message =
                            "رمزها یکسان نیستند"
                    }

                    else -> {

                        activity.changePassword(
                            newPassword
                        )

                        onDone()
                    }
                }
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text("ذخیره رمز جدید")
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        OutlinedButton(
            onClick = onBack
        ) {

            Text("بازگشت")
        }

        if (message.isNotEmpty()) {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = message,
                color =
                    MaterialTheme
                        .colorScheme
                        .error
            )
        }
    }
}


/* =========================================================
   HIDDEN APPS
   ========================================================= */

@Composable
fun HiddenAppsScreen(
    apps: List<InstalledApp>,
    onLaunch: (String) -> Unit,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = "برنامه‌های مخفی",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = onBack
            ) {

                Text("خانه")
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        if (apps.isEmpty()) {

            Box(
                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        "برنامه مخفی پیدا نشد."
                )
            }

        } else {

            LazyVerticalGrid(
                columns =
                    GridCells.Fixed(3),

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp),

                verticalArrangement =
                    Arrangement.spacedBy(20.dp)
            ) {

                items(
                    items = apps,
                    key = { app ->
                        app.packageName
                    }
                ) { app ->

                    AppIcon(
                        app = app,
                        onClick = {
                            onLaunch(
                                app.packageName
                            )
                        }
                    )
                }
            }
        }
    }
}
           