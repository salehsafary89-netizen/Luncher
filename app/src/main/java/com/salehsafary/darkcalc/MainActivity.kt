package com.salehsafary.darkcalc

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val CALCULATOR_CODE = "2580"
private const val LOGIN_USERNAME = "Saleh Safari"
private const val DEFAULT_LOGIN_PASSWORD = "123456789"
private const val SECURITY_ANSWER = "آقای ساعدی"

private val DarkBackground = Color.Black
private val DarkSurface = Color(0xFF151515)
private val DarkSurface2 = Color(0xFF202020)
private val Orange = Color(0xFFFF9800)

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {

    private var apps = mutableStateListOf<InstalledApp>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadApps()

        setContent {
            DarkCalcApp(
                apps = apps,
                onLaunch = { packageName ->
                    launchApp(packageName)
                }
            )
        }
    }

    private fun loadApps() {
        val pm = packageManager

        val loadedApps = pm
            .getInstalledApplications(
                PackageManager.GET_META_DATA
            )
            .filter { app ->
                app.packageName != packageName &&
                    pm.getLaunchIntentForPackage(
                        app.packageName
                    ) != null
            }
            .map { app ->
                InstalledApp(
                    packageName = app.packageName,
                    label = pm
                        .getApplicationLabel(app)
                        .toString(),
                    icon = pm
                        .getApplicationIcon(
                            app.packageName
                        )
                )
            }
            .sortedBy {
                it.label.lowercase(Locale.getDefault())
            }

        apps.clear()
        apps.addAll(loadedApps)
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager
            .getLaunchIntentForPackage(packageName)

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}

@Composable
fun DarkCalcApp(
    apps: List<InstalledApp>,
    onLaunch: (String) -> Unit
) {
    var screen by remember {
        mutableStateOf("home")
    }

    var loginPassword by remember {
        mutableStateOf(DEFAULT_LOGIN_PASSWORD)
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = DarkBackground,
            surface = DarkSurface,
            onBackground = Color.White,
            onSurface = Color.White,
            primary = Orange
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(180)
                    ) togetherWith fadeOut(
                        animationSpec = tween(120)
                    )
                },
                label = "screen_transition"
            ) { currentScreen ->

                when (currentScreen) {

                    "home" -> {
                        HomeScreen(
                            apps = apps,
                            onLaunch = onLaunch,
                            onCalculator = {
                                screen = "calculator"
                            }
                        )
                    }

                    "calculator" -> {
                        CalculatorScreen(
                            onBack = {
                                screen = "home"
                            },
                            onCodeCorrect = {
                                screen = "login"
                            }
                        )
                    }

                    "login" -> {
                        LoginScreen(
                            currentPassword = loginPassword,
                            onBack = {
                                screen = "calculator"
                            },
                            onSuccess = {
                                screen = "home"
                            },
                            onReset = {
                                screen = "reset"
                            }
                        )
                    }

                    "reset" -> {
                        ResetScreen(
                            onBack = {
                                screen = "login"
                            },
                            onDone = { newPassword ->
                                loginPassword = newPassword
                                screen = "login"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    apps: List<InstalledApp>,
    onLaunch: (String) -> Unit,
    onCalculator: () -> Unit
) {
    val visibleApps = remember(apps) {
        apps.filter {
            it.packageName != "com.salehsafary.darkcalc"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {
            LiveClock()

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            LiveDate()
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            horizontalArrangement =
                Arrangement.spacedBy(7.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp),
            contentPadding =
                PaddingValues(
                    horizontal = 3.dp,
                    vertical = 8.dp
                )
        ) {

            item {
                CalculatorAppIcon(
                    onClick = onCalculator
                )
            }

            items(
                items = visibleApps,
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
        color = Color.White,
        fontSize = 46.sp
    )
}

@Composable
fun LiveDate() {
    var date by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        while (true) {

            date = SimpleDateFormat(
                "EEEE، d MMMM",
                Locale("fa")
            ).format(Date())

            delay(60000)
        }
    }

    Text(
        text = date,
        color = Color.White,
        fontSize = 14.sp
    )
}

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

        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(
                    RoundedCornerShape(17.dp)
                )
                .background(
                    DarkSurface2
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Image(
                bitmap = app.icon
                    .toBitmap(
                        width = 128,
                        height = 128
                    )
                    .asImageBitmap(),
                contentDescription =
                    app.label,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = app.label,
            color = Color.White,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

@Composable
fun CalculatorAppIcon(
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

        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(
                    RoundedCornerShape(17.dp)
                )
                .background(Orange),
            contentAlignment =
                Alignment.Center
        ) {

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(
                    text = "+  ×  ÷",
                    color = Color.White,
                    fontSize = 13.sp
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = "▰",
                    color = Color.White,
                    fontSize = 19.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = "ماشین حساب",
            color = Color.White,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

@Composable
fun CalculatorScreen(
    onBack: () -> Unit,
    onCodeCorrect: () -> Unit
) {
    var display by remember {
        mutableStateOf("")
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
            .background(DarkBackground)
            .padding(14.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = "ماشین حساب",
                color = Color.White,
                fontSize = 25.sp
            )

            TextButton(
                onClick = onBack
            ) {
                Text(
                    text = "بازگشت",
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement =
                Arrangement.Bottom
        ) {

            Text(
                text =
                    if (display.isEmpty()) {
                        "0"
                    } else {
                        display
                    },
                color = Color.White,
                fontSize = 43.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 18.dp
                    )
            )
        }

        Column(
            modifier = Modifier.weight(2f)
        ) {

            rows.forEach { row ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {

                    row.forEach { value ->

                        CalculatorButton(
                            value = value,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(3.dp),
                            onClick = {

                                when (value) {

                                    "C" -> {
                                        display = ""
                                    }

                                    "=" -> {

                                        if (
                                            display ==
                                            CALCULATOR_CODE
                                        ) {
                                            display = ""
                                            onCodeCorrect()
                                        } else {
                                            display =
                                                calculateSimple(
                                                    display
                                                )
                                        }
                                    }

                                    else -> {
                                        display += value
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    value: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val isOperator =
        value in listOf(
            "÷",
            "×",
            "−",
            "+"
        )

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor =
                if (isOperator) {
                    Orange
                } else {
                    DarkSurface2
                },
            contentColor = Color.White
        )
    ) {

        Text(
            text = value,
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

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

        when {
            !result.isFinite() -> "خطا"

            result % 1.0 == 0.0 ->
                result.toLong().toString()

            else ->
                result.toString()
        }

    } catch (_: Exception) {
        "خطا"
    }
}

@Composable
fun LoginScreen(
    currentPassword: String,
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
            .background(DarkBackground)
            .padding(26.dp)
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
                color = Color.White,
                fontSize = 30.sp
            )

            Spacer(
                modifier = Modifier.height(28.dp)
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
                    Text(
                        text = "نام کاربری",
                        color = Color.White
                    )
                }
            )

            Spacer(
                modifier = Modifier.height(13.dp)
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
                visualTransformation =
                    PasswordVisualTransformation(),
                label = {
                    Text(
                        text = "رمز عبور",
                        color = Color.White
                    )
                }
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = {

                    if (
                        username.trim() ==
                        LOGIN_USERNAME &&
                        password ==
                        currentPassword
                    ) {

                        error = ""
                        onSuccess()

                    } else {

                        error =
                            "نام کاربری یا رمز عبور اشتباه است"
                    }
                },
                modifier =
                    Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Orange,
                        contentColor = Color.White
                    )
            ) {
                Text(
                    text = "ورود",
                    color = Color.White
                )
            }

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            TextButton(
                onClick = onReset
            ) {
                Text(
                    text = "فراموشی رمز عبور",
                    color = Color.White
                )
            }

            if (error.isNotEmpty()) {

                Spacer(
                    modifier = Modifier.height(7.dp)
                )

                Text(
                    text = error,
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp
                )
            }
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = "بازگشت",
                color = Color.White
            )
        }
    }
}

@Composable
fun ResetScreen(
    onBack: () -> Unit,
    onDone: (String) -> Unit
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
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "بازنشانی رمز",
            color = Color.White,
            fontSize = 27.sp
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text =
                "معلم مورد علاقه‌ات کی بود؟",
            color = Color.White,
            fontSize = 17.sp
        )

        Spacer(
            modifier = Modifier.height(11.dp)
        )

        OutlinedTextField(
            value = answer,
            onValueChange = {
                answer = it
                message = ""
            },
            modifier =
                Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text(
                    text = "پاسخ",
                    color = Color.White
                )
            }
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
            modifier =
                Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation =
                PasswordVisualTransformation(),
            label = {
                Text(
                    text = "رمز جدید",
                    color = Color.White
                )
            }
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
            modifier =
                Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation =
                PasswordVisualTransformation(),
            label = {
                Text(
                    text = "تکرار رمز جدید",
                    color = Color.White
                )
            }
        )

        Spacer(
            modifier = Modifier.height(17.dp)
        )

        Button(
            onClick = {

                when {

                    answer.trim() !=
                        SECURITY_ANSWER -> {

                        message =
                            "پاسخ سؤال اشتباه است"
                    }

                    newPassword.length < 6 -> {

                        message =
                            "رمز باید حداقل ۶ رقم باشد"
                    }

                    newPassword !=
                        confirmPassword -> {

                        message =
                            "رمزها یکسان نیستند"
                    }

                    else -> {
                        onDone(newPassword)
                    }
                }
            },
            modifier =
                Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Orange,
                    contentColor = Color.White
                )
        ) {
            Text(
                text = "ذخیره رمز جدید",
                color = Color.White
            )
        }

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        TextButton(
            onClick = onBack
        ) {
            Text(
                text = "بازگشت",
                color = Color.White
            )
        }

        if (message.isNotEmpty()) {

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = message,
                color = Color(0xFFFF5252),
                fontSize = 13.sp
            )
        }
    }
}

/*
 * DarkCalc launcher notes
 *
 * Home:
 * - همه برنامه‌های قابل اجرا نمایش داده می‌شوند.
 * - ماشین حساب یک آیکن عادی در بین برنامه‌هاست.
 * - App Drawer وجود ندارد.
 * - Dock جداگانه وجود ندارد.
 *
 * Calculator:
 * - کد ورود به Login برابر 2580 است.
 * - ماشین حساب عمداً ساده و سبک نگه داشته شده.
 *
 * Login:
 * - Username: Saleh Safari
 * - Default password: 123456789
 *
 * Recovery:
 * - Question: معلم مورد علاقه‌ات کی بود؟
 * - Answer: آقای ساعدی
 *
 * Theme:
 * - Background: black
 * - Text: white
 * - Calculator accent: orange
 *
 * Layout:
 * - 5 columns
 * - clock/date in upper area
 * - applications in lower area
 *
 * این قسمت از فایل عمداً فقط یک comment است
 * تا فایل همچنان یکپارچه و قابل کامپایل باقی بماند.
 */

/*
 * Lightweight design:
 *
 * هیچ تصویر جدیدی برای آیکن برنامه‌ها تولید نمی‌شود.
 * آیکن واقعی برنامه‌ها مستقیماً از PackageManager
 * گرفته می‌شود.
 *
 * برای جلوگیری از سنگین شدن Launcher:
 *
 * - افکت‌ها کوتاه هستند.
 * - انیمیشن فقط هنگام تغییر صفحه اجرا می‌شود.
 * - ساعت فقط هر ثانیه بروزرسانی می‌شود.
 * - تاریخ هر دقیقه بروزرسانی می‌شود.
 * - از لیست برنامه‌ها فقط یک بار در onCreate
 *   داده‌گیری می‌شود.
 *
 * بنابراین این فایل برای استفاده به عنوان
 * Launcher سبک طراحی شده است.
 */

/*
 * Calculator visual design:
 *
 * آیکن ماشین حساب نارنجی است.
 *
 * نمادهای سفید:
 *
 * +  ×  ÷
 *
 * بالای علامت سفید اصلی قرار می‌گیرند.
 *
 * هدف این است که آیکن شبیه یک ابزار عادی
 * سیستم باشد و ظاهر بیش از حد مشخصی نداشته باشد.
 *
 * داخل ماشین حساب:
 *
 * 1/3 بالایی:
 * نمایش عدد
 *
 * 2/3 پایینی:
 * صفحه کلید
 *
 * دکمه‌های عملیات نارنجی هستند.
 *
 * اعداد و متن‌ها سفید هستند.
 */

/*
 * Authentication flow:
 *
 * Home
 *   ↓
 * Calculator
 *   ↓
 * وارد کردن 2580
 *   ↓
 * Login
 *   ↓
 * Saleh Safari + password
 *   ↓
 * Home
 *
 * مسیر بازیابی:
 *
 * Login
 *   ↓
 * فراموشی رمز عبور
 *   ↓
 * سؤال معلم مورد علاقه
 *   ↓
 * آقای ساعدی
 *   ↓
 * رمز جدید
 *   ↓
 * ورود دوباره
 *
 * رمز جدید فقط در زمان اجرای فعلی برنامه
 * نگهداری می‌شود و با بسته شدن کامل برنامه
 * به مقدار پیش‌فرض برمی‌گردد.
 */

/*
 * پایان MainActivity.kt
 *
 * این فایل باید دقیقاً از قسمت ۱ تا قسمت ۱۰
 * پشت سر هم قرار گرفته باشد.
 *
 * هیچ import اضافی
 * هیچ کلاس اضافی
 * هیچ تابع اضافی
 * و هیچ کد دیگری بین قسمت‌ها قرار نده.
 *
 * ترتیب:
 *
 * 1 package + imports + constants + Activity
 * 2 Home + Clock + Date + App icons
 * 3 Calculator + calculator engine
 * 4 Login
 * 5 Reset
 * 6 توضیحات داخلی فایل
 * 7 توضیحات داخلی فایل
 * 8 توضیحات داخلی فایل
 * 9 توضیحات داخلی فایل
 * 10 پایان فایل
 */