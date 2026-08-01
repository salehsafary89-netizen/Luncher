package com.salehsafary.darkcalc

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val CALCULATOR_CODE = "2580"
private const val LOGIN_USERNAME = "Saleh Safari"
private const val LOGIN_PASSWORD = "123456789"
private const val SECURITY_ANSWER = "آقای سعیدی"

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
                onLaunch = { launchApp(it) }
            )
        }
    }

    private fun loadApps() {
        val pm = packageManager

        val list = pm.getInstalledApplications(
            PackageManager.GET_META_DATA
        )
            .filter {
                pm.getLaunchIntentForPackage(it.packageName) != null
            }
            .map {
                InstalledApp(
                    packageName = it.packageName,
                    label = pm.getApplicationLabel(it).toString(),
                    icon = pm.getApplicationIcon(it.packageName)
                )
            }
            .sortedBy {
                it.label.lowercase()
            }

        apps.clear()
        apps.addAll(list)
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager
            .getLaunchIntentForPackage(packageName)

        if (intent != null) {
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

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color.Black,
            surface = Color(0xFF101010),
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            when (screen) {

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
                        onDone = {
                            screen = "login"
                        }
                    )
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
    val visibleApps = apps
        .filter {
            it.packageName != "com.salehsafary.darkcalc"
        }
        .take(20)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LiveClock()

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            LiveDate()
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(2f),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp),
            verticalArrangement =
                Arrangement.spacedBy(18.dp),
            contentPadding =
                PaddingValues(8.dp)
        ) {

            item {
                AppIcon(
                    label = "ماشین حساب",
                    icon = null,
                    onClick = onCalculator
                )
            }

            items(visibleApps) { app ->

                AppIcon(
                    label = app.label,
                    icon = app.icon,
                    onClick = {
                        onLaunch(app.packageName)
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
    label: String,
    icon: Drawable?,
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
                .size(68.dp)
                .clip(
                    RoundedCornerShape(18.dp)
                )
                .background(
                    Color(0xFF202020)
                ),
            contentAlignment =
                Alignment.Center
        ) {

            if (icon != null) {

                Image(
                    bitmap = icon
                        .toBitmap(
                            width = 128,
                            height = 128
                        )
                        .asImageBitmap(),
                    contentDescription = label,
                    modifier = Modifier
                        .size(54.dp)
                )

            } else {

                Text(
                    text = "▣",
                    color = Color.White,
                    fontSize = 32.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
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
                fontSize = 24.sp
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
                text = if (display.isEmpty()) {
                    "0"
                } else {
                    display
                },
                color = Color.White,
                fontSize = 42.sp,
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
                        Arrangement.spacedBy(8.dp)
                ) {

                    row.forEach { value ->

                        Button(
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
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(3.dp),
                            shape =
                                RoundedCornerShape(18.dp)
                        ) {

                            Text(
                                text = value,
                                fontSize = 22.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
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

        if (!result.isFinite()) {
            "خطا"
        } else if (
            result % 1.0 == 0.0
        ) {
            result.toLong().toString()
        } else {
            result.toString()
        }

    } catch (_: Exception) {
        "خطا"
    }
}

@Composable
fun LoginScreen(
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
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
            label = {
                Text(
                    "نام کاربری",
                    color = Color.White
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                error = ""
            },
            label = {
                Text(
                    "رمز عبور",
                    color = Color.White
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
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
                    LOGIN_PASSWORD
                ) {
                    error = ""
                    onSuccess()
                } else {
                    error =
                        "نام کاربری یا رمز عبور اشتباه است"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ورود")
        }

        TextButton(
            onClick = onReset
        ) {
            Text(
                "فراموشی رمز عبور",
                color = Color.White
            )
        }

        TextButton(
            onClick = onBack
        ) {
            Text(
                "بازگشت",
                color = Color.White
            )
        }

        if (error.isNotEmpty()) {

            Text(
                text = error,
                color = Color.Red,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun ResetScreen(
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    var answer by remember {
        mutableStateOf("")
    }

    var newPassword by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "فراموشی رمز عبور",
            color = Color.White,
            fontSize = 26.sp
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "معلم مورد علاقه‌ات کی بود؟",
            color = Color.White,
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
            label = {
                Text(
                    "پاسخ",
                    color = Color.White
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                message = ""
            },
            label = {
                Text(
                    "رمز جدید",
                    color = Color.White
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = {

                if (
                    answer.trim() ==
                    SECURITY_ANSWER
                ) {

                    if (
                        newPassword.length >= 6
                    ) {
                        message =
                            "رمز جدید ذخیره شد"
                        onDone()
                    } else {
                        message =
                            "رمز باید حداقل ۶ رقم باشد"
                    }

                } else {

                    message =
                        "پاسخ اشتباه است"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ذخیره")
        }

        TextButton(
            onClick = onBack
        ) {
            Text(
                "بازگشت",
                color = Color.White
            )
        }

        if (message.isNotEmpty()) {

            Text(
                text = message,
                color = Color.White
            )
        }
    }
}

/*
 * این بخش عمداً ساده نگه داشته شده تا
 * روی گوشی‌های ضعیف‌تر فشار اضافی ایجاد نکند.
 *
 * هیچ App Drawer جداگانه‌ای وجود ندارد.
 * برنامه‌ها مستقیماً روی Home نمایش داده می‌شوند.
 *
 * ماشین حساب نیز مانند یک آیکن معمولی روی Home است
 * و با وارد کردن 2580 و زدن = وارد Login می‌شود.
 *
 * اطلاعات ورود پیش‌فرض:
 *
 * Username:
 * Saleh Safari
 *
 * Password:
 * 123456789
 *
 * سؤال بازیابی:
 * معلم مورد علاقه‌ات کی بود؟
 *
 * پاسخ:
 * آقای سعیدی
 */

/*
 * پایان MainActivity.kt
 *
 * ترتیب چسباندن:
 *
 * 1
 * 2
 * 3
 * 4
 * 5
 * 6
 * 7
 * 8
 * 9
 * 10
 *
 * هیچ کدی بین این قسمت‌ها قرار نده.
 * هیچ } اضافه‌ای هم بعد از بخش 10 نگذار.
 *
 * کد ماشین حساب:
 * 2580
 *
 * نام کاربری:
 * Saleh Safari
 *
 * رمز:
 * 123456789
 *
 * پاسخ بازیابی:
 * آقای سعیدی
 */