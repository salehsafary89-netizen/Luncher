package com.salehsafary.darkcalc

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import kotlinx.coroutines.delay

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class InstalledApp(
    val label: String,
    val packageName: String,
    val icon: Bitmap,
    val isCalculator: Boolean = false
)


class MainActivity : ComponentActivity() {

    companion object {
        private const val PREFS = "darkcalc"
        private const val PASSWORD = "password"
        private const val DEFAULT_PASSWORD = "1234"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                DarkCalcApp()
            }
        }
    }

    private fun packageNameForSelf(): String {
        return applicationContext.packageName
    }

    fun launchApp(packageName: String) {
        try {
            val intent =
                packageManager.getLaunchIntentForPackage(
                    packageName
                )

            if (intent != null) {
                startActivity(intent)
            }
        } catch (_: Exception) {
        }
    }

    fun checkLogin(
        username: String,
        password: String
    ): Boolean {
        val saved =
            getSharedPreferences(
                PREFS,
                MODE_PRIVATE
            ).getString(
                PASSWORD,
                DEFAULT_PASSWORD
            )

        return username.isNotBlank() &&
                password == saved
    }

    fun changePassword(password: String) {
        getSharedPreferences(
            PREFS,
            MODE_PRIVATE
        )
            .edit()
            .putString(PASSWORD, password)
            .apply()
    }
}

@Composable
fun DarkCalcApp() {

    var screen by remember {
        mutableStateOf("home")
    }

    var apps by remember {
        mutableStateOf<List<InstalledApp>>(
            emptyList()
        )
    }

    val activity =
        androidx.compose.ui.platform.LocalContext
            .current as MainActivity

    LaunchedEffect(Unit) {
        apps = activity.getAppsForUi()
    }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            slideInHorizontally { it } togetherWith
                    slideOutHorizontally { -it }
        },
        label = "screen"
    ) { current ->

        when (current) {

            "home" -> {
                HomeScreen(
                    onApps = {
                        screen = "apps"
                    },
                    onCalculator = {
                        screen = "calculator"
                    }
                )
            }

            "apps" -> {
                AppDrawer(
                    apps = apps,
                    onLaunch = {
                        activity.launchApp(it)
                    },
                    onCalculator = {
                        screen = "calculator"
                    },
                    onBack = {
                        screen = "home"
                    }
                )
            }

            "calculator" -> {
                CalculatorScreen(
                    onBack = {
                        screen = "apps"
                    }
                )
            }
        }
    }
}


private fun MainActivity.getAppsForUi():
        List<InstalledApp> {

    val pm = packageManager

    val intent =
        Intent(Intent.ACTION_MAIN).apply {
            addCategory(
                Intent.CATEGORY_LAUNCHER
            )
        }

    return pm
        .queryIntentActivities(intent, 0)
        .mapNotNull { info ->

            val pkg =
                info.activityInfo.packageName

            if (pkg == packageName) {
                null
            } else {

                val drawable =
                    info.loadIcon(pm)

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

                InstalledApp(
                    label =
                        info.loadLabel(pm)
                            .toString(),
                    packageName = pkg,
                    icon = bitmap
                )
            }
        }
        .distinctBy {
            it.packageName
        }
        .sortedBy {
            it.label.lowercase()
        }
}

@Composable
fun HomeScreen(
    onApps: () -> Unit,
    onCalculator: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme
                    .colorScheme
                    .background
            )
            .padding(20.dp)
    ) {

        Column(
            modifier =
                Modifier.fillMaxSize(),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier =
                    Modifier.height(55.dp)
            )

            LiveClock()

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            LiveDate()

            Spacer(
                modifier =
                    Modifier.height(50.dp)
            )

            Text(
                text = "DarkCalc",
                fontSize = 25.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                LauncherButton(
                    text = "برنامه‌ها",
                    modifier =
                        Modifier.weight(1f),
                    onClick = onApps
                )

                LauncherButton(
                    text = "ماشین حساب",
                    modifier =
                        Modifier.weight(1f),
                    onClick = onCalculator
                )
            }

            Spacer(
                modifier =
                    Modifier.weight(1f)
            )

            Text(
                text = "صفحه اصلی",
                fontSize = 12.sp
            )
        }
    }
}


@Composable
fun LauncherButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier =
            modifier.height(58.dp),
        shape =
            RoundedCornerShape(16.dp)
    ) {

        Text(
            text = text,
            fontSize = 15.sp
        )
    }
}


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
        text = time,
        fontSize = 48.sp,
        fontWeight =
            FontWeight.Light
    )
}


@Composable
fun LiveDate() {

    var date by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        while (true) {

            date =
                SimpleDateFormat(
                    "EEEE، d MMMM",
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

@Composable
fun AppDrawer(
    apps: List<InstalledApp>,
    onLaunch: (String) -> Unit,
    onCalculator: () -> Unit,
    onBack: () -> Unit
) {

    val calculator =
        InstalledApp(
            label = "ماشین حساب",
            packageName =
                "darkcalc.calculator",
            icon =
                createCalculatorIcon(),
            isCalculator = true
        )

    val allApps =
        listOf(calculator) + apps

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

            Text(
                text = "برنامه‌ها",
                fontSize = 26.sp,
                fontWeight =
                    FontWeight.Bold
            )

            OutlinedButton(
                onClick = onBack
            ) {
                Text("خانه")
            }
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        LazyVerticalGrid(
            columns =
                GridCells.Fixed(4),
            modifier =
                Modifier.fillMaxSize(),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp),
            verticalArrangement =
                Arrangement.spacedBy(20.dp)
        ) {

            items(
                items = allApps
            ) { app ->

                AppIcon(
                    app = app,
                    onClick = {

                        if (app.isCalculator) {
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
                }
                .padding(4.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Image(
            bitmap =
                app.icon.asImageBitmap(),
            contentDescription =
                app.label,
            modifier =
                Modifier.size(58.dp)
        )

        Spacer(
            modifier =
                Modifier.height(5.dp)
        )

        Text(
            text = app.label,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

fun createCalculatorIcon(): Bitmap {

    val size = 96

    val bitmap =
        Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.ARGB_8888
        )

    val canvas = Canvas(bitmap)

    val paint =
        android.graphics.Paint(
            android.graphics.Paint
                .ANTI_ALIAS_FLAG
        )

    paint.color =
        android.graphics.Color.DKGRAY

    canvas.drawRect(
        0f,
        0f,
        size.toFloat(),
        size.toFloat(),
        paint
    )

    paint.color =
        android.graphics.Color.WHITE

    paint.textSize = 55f

    paint.textAlign =
        android.graphics.Paint.Align.CENTER

    canvas.drawText(
        "÷",
        size / 2f,
        66f,
        paint
    )

    return bitmap
}


@Composable
fun CalculatorScreen(
    onBack: () -> Unit
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
                display =
                    calculate(display)
            }

            else -> {
                display += value
            }
        }
    }

    val keys =
        listOf(
            listOf(
                "7", "8", "9", "÷"
            ),
            listOf(
                "4", "5", "6", "×"
            ),
            listOf(
                "1", "2", "3", "−"
            ),
            listOf(
                "0", "C", "=", "+"
            )
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(18.dp)
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text = "ماشین حساب",
                fontSize = 25.sp,
                fontWeight =
                    FontWeight.Bold
            )

            OutlinedButton(
                onClick = onBack
            ) {
                Text("برنامه‌ها")
            }
        }

        Spacer(
            modifier =
                Modifier.height(30.dp)
        )

        Text(
            text =
                if (display.isEmpty())
                    "0"
                else
                    display,
            fontSize = 40.sp,
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        keys.forEach { row ->

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                row.forEach { key ->

                    Button(
                        onClick = {
                            press(key)
                        },
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(60.dp)
                    ) {

                        Text(
                            text = key,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )
        }
    }
}


fun calculate(
    expression: String
): String {

    if (expression.isBlank()) {
        return "0"
    }

    return try {

        val e =
            expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")

        val op =
            listOf(
                '+',
                '-',
                '*',
                '/'
            ).firstOrNull {
                e.contains(it)
            }
                ?: return e

        val parts =
            e.split(op)

        if (parts.size != 2) {
            return "خطا"
        }

        val a =
            parts[0]
                .trim()
                .toDouble()

        val b =
            parts[1]
                .trim()
                .toDouble()

        val result =
            when (op) {

                '+' -> {
                    a + b
                }

                '-' -> {
                    a - b
                }

                '*' -> {
                    a * b
                }

                '/' -> {

                    if (b == 0.0) {
                        return "خطا"
                    }

                    a / b
                }

                else -> {
                    return "خطا"
                }
            }

        if (!result.isFinite()) {
            "خطا"
        } else if (
            result % 1.0 == 0.0
        ) {
            result
                .toLong()
                .toString()
        } else {
            result.toString()
        }

    } catch (_: Exception) {

        "خطا"
    }
}

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
        modifier =
            Modifier
                .fillMaxSize()
                .imePadding()
                .padding(
                    horizontal = 28.dp
                )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "ورود",
                fontSize = 30.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    error = ""
                },
                modifier =
                    Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("نام کاربری")
                }
            )

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    error = ""
                },
                modifier =
                    Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("رمز عبور")
                },
                visualTransformation =
                    PasswordVisualTransformation()
            )

            Spacer(
                modifier =
                    Modifier.height(22.dp)
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
                modifier =
                    Modifier
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
                modifier =
                    Modifier.height(8.dp)
            )

            TextButton(
                onClick = onReset
            ) {

                Text(
                    text =
                        "فراموشی رمز عبور",
                    fontSize = 14.sp
                )
            }

            if (error.isNotEmpty()) {

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
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
            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .padding(
                        bottom = 18.dp
                    )
        ) {

            Text("بازگشت")
        }
    }
}

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
        modifier =
            Modifier
                .fillMaxSize()
                .imePadding()
                .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier =
                Modifier.height(55.dp)
        )

        Text(
            text = "بازنشانی رمز",
            fontSize = 27.sp,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Text(
            text =
                "پاسخ امنیتی",
            fontSize = 17.sp
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = answer,
            onValueChange = {
                answer = it
                message = ""
            },
            modifier =
                Modifier.fillMaxWidth(),
            label = {
                Text("پاسخ")
            },
            singleLine = true
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                message = ""
            },
            modifier =
                Modifier.fillMaxWidth(),
            label = {
                Text("رمز جدید")
            },
            singleLine = true,
            visualTransformation =
                PasswordVisualTransformation()
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                message = ""
            },
            modifier =
                Modifier.fillMaxWidth(),
            label = {
                Text("تکرار رمز")
            },
            singleLine = true,
            visualTransformation =
                PasswordVisualTransformation()
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Button(
            onClick = {

                if (answer.isBlank()) {

                    message =
                        "پاسخ را وارد کنید"

                } else if (
                    newPassword.length < 4
                ) {

                    message =
                        "رمز کوتاه است"

                } else if (
                    newPassword !=
                    confirmPassword
                ) {

                    message =
                        "رمزها یکسان نیستند"

                } else {

                    activity.changePassword(
                        newPassword
                    )

                    onDone()
                }
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text("ذخیره رمز جدید")
        }

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        OutlinedButton(
            onClick = onBack
        ) {

            Text("بازگشت")
        }

        if (message.isNotEmpty()) {

            Spacer(
                modifier =
                    Modifier.height(12.dp)
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

@Composable
fun HiddenAppsScreen(
    apps: List<InstalledApp>,
    onLaunch: (String) -> Unit,
    onBack: () -> Unit
) {

    Column(
        modifier =
            Modifier
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
                text =
                    "برنامه‌های مخفی",
                fontSize = 25.sp,
                fontWeight =
                    FontWeight.Bold
            )

            OutlinedButton(
                onClick = onBack
            ) {

                Text("خانه")
            }
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
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
                    key = {
                        it.packageName
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


/*
 * این بخش فقط برای اینکه فایل
 * در صورت استفاده از صفحه ورود
 * تابع‌های لازم را داشته باشد.
 */

fun MainActivity.checkSecurityAnswer(
    answer: String
): Boolean {

    return answer.isNotBlank()
}


/*
 * پایان قابلیت‌های ورود.
 */

/*
 * پایان فایل MainActivity.kt
 *
 * نکته:
 * ماشین حساب در AppDrawer به عنوان
 * یک آیکون مستقل نمایش داده می‌شود
 * و داخل Dock قرار نگرفته است.
 *
 * صفحه‌ها نیز با AnimatedContent
 * به صورت افقی جابه‌جا می‌شوند.
 */


/*
 * اگر بعداً خواستی LoginScreen را به
 * مسیر اصلی برنامه وصل کنی، فقط state
 * مربوط به screen در DarkCalcApp را
 * تغییر بده.
 *
 * فعلاً این نسخه عمداً ساده نگه داشته
 * شده تا ساختار اصلی بدون خطا باشد.
 */