package com.salehsafary.darkcalc

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private const val CALCULATOR_CODE = "2580"
private const val LOGIN_USERNAME = "Saleh Safari"
private const val LOGIN_PASSWORD = "123456789"
private const val SECURITY_ANSWER = "آقای سعیدی"

data class InstalledApp(
    val label: String,
    val packageName: String,
    val icon: Bitmap
)

enum class Screen {
    HOME,
    CALCULATOR,
    LOGIN
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DarkCalcApp(this)
        }
    }

    fun getInstalledApps(): List<InstalledApp> {
        val pm = packageManager

        return pm.getInstalledApplications(
            PackageManager.GET_META_DATA
        )
            .filter {
                pm.getLaunchIntentForPackage(it.packageName) != null
            }
            .mapNotNull { info ->
                try {
                    val drawable = pm.getApplicationIcon(info.packageName)
                    val bitmap = when (drawable) {
                        is BitmapDrawable ->
                            drawable.bitmap

                        else -> {
                            val bitmap = Bitmap.createBitmap(
                                96,
                                96,
                                Bitmap.Config.ARGB_8888
                            )
                            val canvas =
                                android.graphics.Canvas(bitmap)
                            drawable.setBounds(
                                0,
                                0,
                                canvas.width,
                                canvas.height
                            )
                            drawable.draw(canvas)
                            bitmap
                        }
                    }

                    InstalledApp(
                        label = pm.getApplicationLabel(info)
                            .toString(),
                        packageName = info.packageName,
                        icon = bitmap
                    )
                } catch (_: Exception) {
                    null
                }
            }
            .sortedBy {
                it.label.lowercase(Locale.getDefault())
            }
    }

    fun launchApp(packageName: String) {
        try {
            val intent =
                packageManager.getLaunchIntentForPackage(
                    packageName
                )

            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            if (intent != null) {
                startActivity(intent)
            }
        } catch (_: Exception) {
        }
    }
}

@Composable
fun DarkCalcApp(activity: MainActivity) {

    var screen by remember {
        mutableStateOf(Screen.HOME)
    }

    val apps = remember {
        activity.getInstalledApps()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color.Black,
            surface = Color(0xFF111111),
            primary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen"
            ) { current ->

                when (current) {
                    Screen.HOME -> {
                        HomeScreen(
                            apps = apps,
                            onCalculator = {
                                screen = Screen.CALCULATOR
                            },
                            onLaunch = {
                                activity.launchApp(it)
                            }
                        )
                    }

                    Screen.CALCULATOR -> {
                        CalculatorScreen(
                            onBack = {
                                screen = Screen.HOME
                            },
                            onLogin = {
                                screen = Screen.LOGIN
                            }
                        )
                    }

                    Screen.LOGIN -> {
                        LoginScreen(
                            onBack = {
                                screen = Screen.CALCULATOR
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
    onCalculator: () -> Unit,
    onLaunch: (String) -> Unit
) {
    val visibleApps = remember(apps) {
        apps.take(24)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(
                horizontal = 14.dp,
                vertical = 18.dp
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.33f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                LiveClock()

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                LiveDate()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.67f)
        ) {

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp),
                verticalArrangement =
                    Arrangement.spacedBy(18.dp),
                contentPadding =
                    PaddingValues(bottom = 10.dp)
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
        fontSize = 48.sp,
        fontWeight = FontWeight.Light
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

        Image(
            bitmap = app.icon.asImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier
                .size(64.dp)
                .clip(
                    RoundedCornerShape(18.dp)
                )
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = app.label,
            color = Color.White,
            fontSize = 10.sp,
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
                .size(64.dp)
                .clip(
                    RoundedCornerShape(18.dp)
                )
                .background(
                    Color(0xFF303030)
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = "▦",
                    color = Color.White,
                    fontSize = 28.sp
                )

                Text(
                    text = "123",
                    color = Color.LightGray,
                    fontSize = 7.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = "Calculator",
            color = Color.White,
            fontSize = 10.sp
        )
    }
}

@Composable
fun CalculatorScreen(
    onBack: () -> Unit,
    onLogin: () -> Unit
) {

    var display by remember {
        mutableStateOf("")
    }

    fun press(value: String) {

        when (value) {

            "C" -> {
                display = ""
            }

            "⌫" -> {
                if (display.isNotEmpty()) {
                    display = display.dropLast(1)
                }
            }

            "=" -> {

                if (display == CALCULATOR_CODE) {
                    display = ""
                    onLogin()
                } else {
                    display =
                        calculateSimple(display)
                }
            }

            else -> {
                if (display.length < 20) {
                    display += value
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(12.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.32f),
            contentAlignment =
                Alignment.BottomEnd
        ) {

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {

                Text(
                    text = "ماشین حساب",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        if (display.isEmpty())
                            "0"
                        else
                            display,
                    color = Color.White,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        CalculatorKeyboard(
            onPress = ::press
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        TextButton(
            onClick = onBack,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        ) {
            Text(
                text = "خانه",
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CalculatorKeyboard(
    onPress: (String) -> Unit
) {

    val rows = listOf(
        listOf("C", "⌫", "÷", "×"),
        listOf("7", "8", "9", "−"),
        listOf("4", "5", "6", "+"),
        listOf("1", "2", "3", "="),
        listOf("0", ".", "(", ")")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(0.68f),
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        rows.forEach { row ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                row.forEach { value ->

                    CalculatorButton(
                        text = value,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onPress(value)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val special =
        text == "=" ||
        text == "÷" ||
        text == "×" ||
        text == "−" ||
        text == "+"

    Button(
        onClick = onClick,
        modifier = modifier
            .height(62.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor =
                if (special)
                    Color(0xFF3D3D3D)
                else
                    Color(0xFF1D1D1D),
            contentColor = Color.White
        )
    ) {

        Text(
            text = text,
            fontSize = 21.sp,
            fontWeight =
                if (text == "=")
                    FontWeight.Bold
                else
                    FontWeight.Normal
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

        val value = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")

        val operator =
            listOf('+', '-', '*', '/')
                .firstOrNull {
                    value.contains(it)
                }
                ?: return value

        val parts =
            value.split(operator)

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
    onBack: () -> Unit
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

    var recovery by remember {
        mutableStateOf(false)
    }

    var answer by remember {
        mutableStateOf("")
    }

    var newPassword by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text =
                if (recovery)
                    "بازیابی رمز"
                else
                    "ورود",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        if (!recovery) {

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
                        "نام کاربری",
                        color = Color.Gray
                    )
                },
                colors =
                    loginFieldColors()
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
                modifier =
                    Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text(
                        "رمز عبور",
                        color = Color.Gray
                    )
                },
                visualTransformation =
                    PasswordVisualTransformation(),
                colors =
                    loginFieldColors()
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

                        error = "ورود موفق بود"

                    } else {

                        error =
                            "نام کاربری یا رمز عبور اشتباه است"
                    }
                },
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "ورود",
                    color = Color.Black
                )
            }

            TextButton(
                onClick = {
                    recovery = true
                    error = ""
                }
            ) {

                Text(
                    "فراموشی رمز عبور",
                    color = Color.White
                )
            }

        } else {

            Text(
                text =
                    "معلم مورد علاقه‌ات کی بود؟",
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = answer,
                onValueChange = {
                    answer = it
                    error = ""
                },
                modifier =
                    Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text(
                        "پاسخ",
                        color = Color.Gray
                    )
                },
                colors =
                    loginFieldColors()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    error = ""
                },
                modifier =
                    Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text(
                        "رمز جدید",
                        color = Color.Gray
                    )
                },
                visualTransformation =
                    PasswordVisualTransformation(),
                colors =
                    loginFieldColors()
            )

            Spacer(
                modifier = Modifier.height(18.dp)
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
                            error =
                                "رمز جدید ثبت شد"
                        } else {
                            error =
                                "رمز باید حداقل ۶ رقم باشد"
                        }

                    } else {

                        error =
                            "پاسخ سؤال اشتباه است"
                    }
                },
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "بازنشانی رمز",
                    color = Color.Black
                )
            }

            TextButton(
                onClick = {
                    recovery = false
                    error = ""
                }
            ) {

                Text(
                    "بازگشت به ورود",
                    color = Color.White
                )
            }
        }

        if (error.isNotEmpty()) {

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = error,
                color =
                    if (
                        error == "ورود موفق بود" ||
                        error == "رمز جدید ثبت شد"
                    )
                        Color(0xFF66BB6A)
                    else
                        Color(0xFFFF6666),
                fontSize = 13.sp
            )
        }

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        TextButton(
            onClick = onBack
        ) {

            Text(
                "بازگشت",
                color = Color.Gray
            )
        }
    }
}

@Composable
fun loginFieldColors(): OutlinedTextFieldColors {

    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color.White,
        unfocusedBorderColor =
            Color(0xFF555555),
        cursorColor = Color.White
    )
}

@Composable
fun HomeAppIconCard(
    app: InstalledApp,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                Color(0xFF151515)
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 10.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Image(
                bitmap =
                    app.icon.asImageBitmap(),
                contentDescription =
                    app.label,
                modifier = Modifier
                    .size(62.dp)
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = app.label,
                color = Color.White,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CalculatorLogo(
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(
                Color(0xFF303030)
            ),
        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(18.dp)
                    .clip(
                        RoundedCornerShape(4.dp)
                    )
                    .background(
                        Color(0xFF111111)
                    )
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {

                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(
                                RoundedCornerShape(3.dp)
                            )
                            .background(
                                Color.White
                            )
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {

                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(
                                RoundedCornerShape(3.dp)
                            )
                            .background(
                                Color.LightGray
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun SimplePageMotion(
    content: @Composable () -> Unit
) {

    AnimatedContent(
        targetState = true,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "page_motion"
    ) {
        content()
    }
}

@Composable
fun SectionTitle(
    title: String
) {

    Text(
        text = title,
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun EmptyMessage(
    text: String
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text = text,
            color = Color.Gray,
            fontSize = 15.sp
        )
    }
}

@Composable
fun DarkButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor =
                Color(0xFF222222),
            contentColor = Color.White
        )
    ) {

        Text(
            text = text,
            fontSize = 15.sp
        )
    }
}

@Composable
fun WhiteButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {

        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DarkCalcPreview() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        CalculatorLogo(
            modifier = Modifier.size(80.dp)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Calculator",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

