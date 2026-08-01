package com.salehsafary.darkcalc

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

private const val SECRET_CODE = "2580"
private const val PREFS = "darkcalc_prefs"
private const val KEY_PASSWORD = "password"

private const val SECURITY_QUESTION =
    "نام اولین مدرسه شما چه بود؟"

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Bitmap,
    val isCalculator: Boolean = false
)

class MainActivity : ComponentActivity() {

    private var installedApps by mutableStateOf<List<InstalledApp>>(emptyList())

    private var currentScreen by mutableStateOf("apps")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installedApps = loadApps()

        setContent {
            MaterialTheme {
                DarkCalcApp()
            }
        }
    }

    private fun loadApps(): List<InstalledApp> {
        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val result = mutableListOf<InstalledApp>()

        val infos = pm.queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        )

        infos
            .sortedBy {
                it.loadLabel(pm).toString().lowercase()
            }
            .forEach { info ->

                val packageName =
                    info.activityInfo.packageName

                if (packageName != packageName()) {
                    val drawable =
                        info.loadIcon(pm)

                    val bitmap =
                        if (drawable is BitmapDrawable) {
                            drawable.bitmap
                        } else {
                            Bitmap.createBitmap(
                                96,
                                96,
                                Bitmap.Config.ARGB_8888
                            )
                        }

                    result.add(
                        InstalledApp(
                            packageName = packageName,
                            label = info.loadLabel(pm).toString(),
                            icon = bitmap
                        )
                    )
                }
            }

        val calculatorIcon =
            Bitmap.createBitmap(
                96,
                96,
                Bitmap.Config.ARGB_8888
            )

        result.add(
            0,
            InstalledApp(
                packageName = "darkcalc.calculator",
                label = "ماشین حساب",
                icon = calculatorIcon,
                isCalculator = true
            )
        )

        return result
    }

    private fun packageName(): String {
        return applicationContext.packageName
    }

    private fun launchApp(packageName: String) {
        val intent =
            packageManager.getLaunchIntentForPackage(
                packageName
            )

        if (intent != null) {
            startActivity(intent)
        }
    }

    fun checkLogin(
        username: String,
        password: String
    ): Boolean {

        val savedPassword =
            getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            ).getString(
                KEY_PASSWORD,
                "123456"
            )

        return username.isNotBlank() &&
                password == savedPassword
    }

    fun checkSecurityAnswer(
        answer: String
    ): Boolean {
        return answer.trim() == "مدرسه"
    }

    fun changePassword(
        password: String
    ) {
        getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                KEY_PASSWORD,
                password
            )
            .apply()
    }

    @Composable
    private fun DarkCalcApp() {

        when (currentScreen) {

            "apps" -> {
                AppHomeScreen(
                    apps = installedApps,
                    onLaunch = {
                        launchApp(it)
                    },
                    onCalculator = {
                        currentScreen = "calculator"
                    }
                )
            }

            "calculator" -> {
                CalculatorScreen(
                    onBack = {
                        currentScreen = "apps"
                    },
                    onSecretCode = {
                        currentScreen = "login"
                    }
                )
            }

            "login" -> {
                LoginScreen(
                    activity = this,
                    onBack = {
                        currentScreen = "calculator"
                    },
                    onSuccess = {
                        currentScreen = "hidden"
                    },
                    onReset = {
                        currentScreen = "reset"
                    }
                )
            }

            "reset" -> {
                ResetPasswordScreen(
                    activity = this,
                    onBack = {
                        currentScreen = "login"
                    },
                    onDone = {
                        currentScreen = "login"
                    }
                )
            }

            "hidden" -> {
                HiddenAppsScreen(
                    apps = emptyList(),
                    onLaunch = {
                        launchApp(it)
                    },
                    onBack = {
                        currentScreen = "apps"
                    }
                )
            }
        }
    }
}

@Composable
fun AppHomeScreen(
    apps: List<InstalledApp>,
    onLaunch: (String) -> Unit,
    onCalculator: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 12.dp,
                vertical = 18.dp
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            LiveClock()

            LiveDate()

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = 24.dp,
                    start = 4.dp,
                    end = 4.dp
                ),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp),
                verticalArrangement =
                    Arrangement.spacedBy(24.dp)
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
                .size(64.dp)
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant,
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment =
                Alignment.Center
        ) {

            if (app.isCalculator) {

                Text(
                    text = "⌨",
                    fontSize = 32.sp
                )

            } else {

                Image(
                    bitmap =
                        app.icon.asImageBitmap(),
                    contentDescription =
                        app.label,
                    modifier =
                        Modifier.size(58.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = app.label,
            fontSize = 11.sp,
            maxLines = 1
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
        fontSize = 44.sp,
        modifier = Modifier.fillMaxWidth()
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
                    "EEEE، d MMMM yyyy",
                    Locale("fa")
                ).format(Date())

            delay(60000)
        }
    }

    Text(
        text = date,
        fontSize = 14.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

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

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    if (display.isEmpty()) {
                        "0"
                    } else {
                        display
                    },
                fontSize = 42.sp,
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            rows.forEach { row ->

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    row.forEach { value ->

                        Button(
                            onClick = {
                                press(value)
                            },
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(64.dp)
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

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            TextButton(
                onClick = onBack
            ) {
                Text("برنامه‌ها")
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

        val normalized =
            expression
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

        val result =
            when (operator) {

                '+' -> first + second

                '-' -> first - second

                '*' -> first * second

                '/' -> {

                    if (second == 0.0) {
                        return "خطا"
                    }

                    first / second
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
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "ورود",
                fontSize = 30.sp,
                fontWeight =
                    androidx.compose.ui.text.font
                        .FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(30.dp)
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
                    .height(56.dp),
                shape =
                    RoundedCornerShape(12.dp)
            ) {

                Text(
                    text = "ورود",
                    fontSize = 17.sp
                )
            }

            Spacer(
                modifier = Modifier.height(6.dp)
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
                    modifier = Modifier.height(8.dp)
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

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            TextButton(
                onClick = onBack
            ) {
                Text("بازگشت")
            }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "بازنشانی رمز",
                fontSize = 28.sp,
                fontWeight =
                    androidx.compose.ui.text.font
                        .FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            Text(
                text = SECURITY_QUESTION,
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.height(14.dp)
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
                modifier = Modifier.height(18.dp)
            )

            Button(
                onClick = {

                    when {

                        !activity
                            .checkSecurityAnswer(
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
                    modifier = Modifier.height(10.dp)
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
}

@Composable
fun HiddenAppsScreen(
    apps: List<InstalledApp>,
    onLaunch: (String) -> Unit,
    onBack: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
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
                    fontWeight =
                        androidx.compose.ui.text.font
                            .FontWeight.Bold
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
                        GridCells.Fixed(4),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(22.dp)
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
}

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

            Text(
                text =
                    if (index == current) {
                        "●"
                    } else {
                        "○"
                    },
                fontSize = 11.sp,
                modifier =
                    Modifier.padding(
                        horizontal = 3.dp
                    )
            )
        }
    }
}

@Composable
fun SimpleBackButton(
    text: String = "بازگشت",
    onClick: () -> Unit
) {

    TextButton(
        onClick = onClick
    ) {

        Text(text)
    }
}

private fun normalizeInput(
    value: String
): String {

    return value
        .replace("۰", "0")
        .replace("۱", "1")
        .replace("۲", "2")
        .replace("۳", "3")
        .replace("۴", "4")
        .replace("۵", "5")
        .replace("۶", "6")
        .replace("۷", "7")
        .replace("۸", "8")
        .replace("۹", "9")
}

private fun createCalculatorBitmap(): Bitmap {

    return Bitmap.createBitmap(
        96,
        96,
        Bitmap.Config.ARGB_8888
    )
}

private fun safeLabel(
    context: Context,
    packageName: String
): String {

    return try {

        val info =
            context.packageManager
                .getApplicationInfo(
                    packageName,
                    0
                )

        context.packageManager
            .getApplicationLabel(info)
            .toString()

    } catch (_: Exception) {

        packageName
    }
}

private fun safeIcon(
    context: Context,
    packageName: String
): Bitmap {

    return try {

        val drawable =
            context.packageManager
                .getApplicationIcon(
                    packageName
                )

        if (drawable is BitmapDrawable) {

            drawable.bitmap

        } else {

            Bitmap.createBitmap(
                96,
                96,
                Bitmap.Config.ARGB_8888
            )
        }

    } catch (_: Exception) {

        Bitmap.createBitmap(
            96,
            96,
            Bitmap.Config.ARGB_8888
        )
    }
}

@Composable
fun CenteredTitle(
    text: String
) {

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            fontSize = 26.sp,
            fontWeight =
                androidx.compose.ui.text.font
                    .FontWeight.Bold
        )
    }
}

@Composable
fun EmptyState(
    text: String
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            fontSize = 16.sp
        )
    }
}