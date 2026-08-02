package com.salehsafary.darkcalc

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val CALCULATOR_CODE = "2580"
private const val DEFAULT_USERNAME = "Saleh Safari"
private const val DEFAULT_PASSWORD = "123456789"
private const val SECURITY_ANSWER = "آقای ساعدی"

private val BG = Color.Black
private val SURFACE = Color(0xFF171717)
private val SURFACE2 = Color(0xFF252525)
private val ORANGE = Color(0xFFFF9800)
private val WHITE = Color.White

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {

    private val apps = mutableStateListOf<InstalledApp>()
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(
            "darkcalc",
            MODE_PRIVATE
        )

        loadApps()

        setContent {
            DarkCalcApp(
                apps = apps,
                prefs = prefs,
                onLaunch = ::launchApp
            )
        }
    }

    private fun loadApps() {
        val pm = packageManager

        val result =
            pm.getInstalledApplications(
                PackageManager.GET_META_DATA
            )
                .filter {
                    it.packageName != packageName &&
                        pm.getLaunchIntentForPackage(
                            it.packageName
                        ) != null
                }
                .map {
                    InstalledApp(
                        packageName = it.packageName,
                        label = pm.getApplicationLabel(it)
                            .toString(),
                        icon = pm.getApplicationIcon(
                            it.packageName
                        )
                    )
                }
                .sortedBy {
                    it.label.lowercase(
                        Locale.getDefault()
                    )
                }

        apps.clear()
        apps.addAll(result)
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
}

@Composable
fun HomeScreen(
    apps: List<InstalledApp>,
    prefs: SharedPreferences,
    onLaunch: (String) -> Unit,
    onCalculator: () -> Unit
) {
    val hidden = remember {
        mutableStateOf(
            prefs.getStringSet(
                "hidden_apps",
                emptySet()
            ) ?: emptySet()
        )
    }

    val visibleApps = remember(
        apps,
        hidden.value
    ) {
        apps.filter {
            !hidden.value.contains(
                it.packageName
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
            .padding(horizontal = 10.dp)
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
            AnalogClock()

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            PersianDate()
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            horizontalArrangement =
                Arrangement.spacedBy(6.dp),
            verticalArrangement =
                Arrangement.spacedBy(18.dp),
            contentPadding =
                PaddingValues(
                    top = 8.dp,
                    bottom = 18.dp
                )
        ) {

            item(
                key = "darkcalc_calculator"
            ) {
                CalculatorIcon(
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
fun AnalogClock() {
    var now by remember {
        mutableStateOf(Date())
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(1000)
        }
    }

    val time =
        SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        ).format(now)

    Box(
        modifier = Modifier
            .size(125.dp)
            .clip(
                RoundedCornerShape(70.dp)
            )
            .background(SURFACE),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = "◷",
                color = WHITE,
                fontSize = 48.sp
            )

            Text(
                text = time,
                color = ORANGE,
                fontSize = 18.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
fun PersianDate() {
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
        color = WHITE,
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
            .animateContentSize()
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
                    RoundedCornerShape(18.dp)
                )
                .background(SURFACE2),
            contentAlignment =
                Alignment.Center
        ) {
            Image(
                bitmap = app.icon
                    .toBitmap(128, 128)
                    .asImageBitmap(),
                contentDescription =
                    app.label,
                modifier =
                    Modifier.size(52.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = app.label,
            color = WHITE,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

@Composable
fun CalculatorIcon(
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
                    RoundedCornerShape(18.dp)
                )
                .background(ORANGE),
            contentAlignment =
                Alignment.Center
        ) {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    text = "+  ×  ÷",
                    color = WHITE,
                    fontSize = 11.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text = "▰",
                    color = WHITE,
                    fontSize = 21.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "ماشین حساب",
            color = WHITE,
            fontSize = 9.sp
        )
    }
}

@Composable
fun CalculatorScreen(
    onBack: () -> Unit,
    onCorrectCode: () -> Unit
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
            .background(BG)
            .padding(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Text(
                text = "ماشین حساب",
                color = WHITE,
                fontSize = 24.sp,
                fontWeight =
                    FontWeight.Bold
            )

            TextButton(
                onClick = onBack
            ) {
                Text(
                    text = "بازگشت",
                    color = WHITE
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment =
                Alignment.BottomEnd
        ) {
            Text(
                text =
                    if (display.isEmpty())
                        "0"
                    else
                        display,
                color = WHITE,
                fontSize = 42.sp
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
                        Arrangement.spacedBy(6.dp)
                ) {
                    row.forEach { value ->
                        CalculatorButton(
                            value = value,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(3.dp)
                        ) {
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
                                        onCorrectCode()
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
    val operator =
        value in listOf(
            "+",
            "−",
            "×",
            "÷"
        )

    Button(
        onClick = onClick,
        modifier = modifier,
        shape =
            RoundedCornerShape(18.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    if (operator)
                        ORANGE
                    else
                        SURFACE2,
                contentColor = WHITE
            )
    ) {
        Text(
            text = value,
            color = WHITE,
            fontSize = 21.sp
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
        val normalized =
            expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")

        val operator =
            listOf(
                '+',
                '-',
                '*',
                '/'
            ).firstOrNull {
                normalized.contains(it)
            } ?: return normalized

        val parts =
            normalized.split(operator)

        if (parts.size != 2) {
            return "خطا"
        }

        val first =
            parts[0]
                .trim()
                .toDouble()

        val second =
            parts[1]
                .trim()
                .toDouble()

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
    password: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onReset: () -> Unit
) {
    var username by remember {
        mutableStateOf("")
    }

    var pass by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "ورود",
            color = WHITE,
            fontSize = 30.sp,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(24.dp)
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
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = pass,
            onValueChange = {
                pass = it
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
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = {
                if (
                    username.trim() ==
                    DEFAULT_USERNAME &&
                    pass == password
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
                    containerColor = ORANGE
                )
        ) {
            Text(
                text = "ورود",
                color = WHITE
            )
        }

        TextButton(
            onClick = onReset
        ) {
            Text(
                text = "فراموشی رمز عبور",
                color = WHITE
            )
        }

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = ORANGE,
                fontSize = 13.sp
            )
        }

        TextButton(
            onClick = onBack
        ) {
            Text(
                text = "بازگشت",
                color = WHITE
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

    var confirm by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "بازنشانی رمز",
            color = WHITE,
            fontSize = 27.sp,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Text(
            text =
                "معلم مورد علاقه‌ات کی بود؟",
            color = WHITE,
            fontSize = 16.sp
        )

        Spacer(
            modifier = Modifier.height(10.dp)
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
                Text("پاسخ")
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
            label = {
                Text("رمز جدید")
            },
            visualTransformation =
                PasswordVisualTransformation()
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = confirm,
            onValueChange = {
                confirm = it
                message = ""
            },
            modifier =
                Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text("تکرار رمز")
            },
            visualTransformation =
                PasswordVisualTransformation()
        )

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        Button(
            onClick = {
                when {
                    answer.trim() !=
                        SECURITY_ANSWER -> {
                        message =
                            "پاسخ اشتباه است"
                    }

                    newPassword.length < 6 -> {
                        message =
                            "رمز باید حداقل ۶ کاراکتر باشد"
                    }

                    newPassword != confirm -> {
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
                    containerColor = ORANGE
                )
        ) {
            Text(
                text = "ذخیره رمز جدید",
                color = WHITE
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        TextButton(
            onClick = onBack
        ) {
            Text(
                text = "بازگشت",
                color = WHITE
            )
        }

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = ORANGE,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun HiddenAppsScreen(
    apps: List<InstalledApp>,
    prefs: SharedPreferences,
    onBack: () -> Unit,
    onLaunch: (String) -> Unit
) {
    var hidden by remember {
        mutableStateOf(
            prefs.getStringSet(
                "hidden_apps",
                emptySet()
            ) ?: emptySet()
        )
    }

    var selecting by remember {
        mutableStateOf(false)
    }

    val hiddenApps =
        apps.filter {
            hidden.contains(
                it.packageName
            )
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
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
                text =
                    if (selecting)
                        "انتخاب برنامه"
                    else
                        "برنامه‌های مخفی",
                color = WHITE,
                fontSize = 24.sp,
                fontWeight =
                    FontWeight.Bold
            )

            TextButton(
                onClick = {
                    selecting = false

                    // خروج = دوباره Login
                    onBack()
                }
            ) {
                Text(
                    text = "خروج",
                    color = WHITE
                )
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        if (!selecting) {

            Text(
                text =
                    if (hiddenApps.isEmpty())
                        "هنوز برنامه‌ای مخفی نشده"
                    else
                        "برنامه‌های مخفی‌شده",
                color = ORANGE,
                fontSize = 15.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            if (hiddenApps.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            "برای انتخاب برنامه\n" +
                            "دکمه پایین را بزن",
                        color = WHITE,
                        fontSize = 14.sp
                    )
                }

            } else {

                LazyVerticalGrid(
                    columns =
                        GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(18.dp),
                    contentPadding =
                        PaddingValues(
                            top = 8.dp,
                            bottom = 12.dp
                        )
                ) {

                    items(
                        items = hiddenApps,
                        key = {
                            it.packageName
                        }
                    ) { app ->

                        HiddenAppItem(
                            app = app,
                            onLaunch = {
                                onLaunch(
                                    app.packageName
                                )
                            },
                            onRemove = {

                                val newSet =
                                    hidden.toMutableSet()

                                newSet.remove(
                                    app.packageName
                                )

                                hidden = newSet

                                prefs.edit()
                                    .putStringSet(
                                        "hidden_apps",
                                        newSet
                                    )
                                    .apply()
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(
                onClick = {
                    selecting = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape =
                    RoundedCornerShape(15.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = ORANGE
                    )
            ) {
                Text(
                    text =
                        "برنامه‌های بیشتر…",
                    color = WHITE,
                    fontSize = 16.sp
                )
            }

        } else {

            Text(
                text =
                    "برنامه‌هایی را انتخاب کن",
                color = WHITE,
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            LazyVerticalGrid(
                columns =
                    GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp),
                verticalArrangement =
                    Arrangement.spacedBy(18.dp),
                contentPadding =
                    PaddingValues(
                        top = 8.dp,
                        bottom = 12.dp
                    )
            ) {

                items(
                    items = apps,
                    key = {
                        it.packageName
                    }
                ) { app ->

                    SelectableAppItem(
                        app = app,
                        selected =
                            hidden.contains(
                                app.packageName
                            ),
                        onClick = {

                            val newSet =
                                hidden.toMutableSet()

                            if (
                                newSet.contains(
                                    app.packageName
                                )
                            ) {
                                newSet.remove(
                                    app.packageName
                                )
                            } else {
                                newSet.add(
                                    app.packageName
                                )
                            }

                            hidden = newSet

                            prefs.edit()
                                .putStringSet(
                                    "hidden_apps",
                                    newSet
                                )
                                .apply()
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(
                onClick = {
                    selecting = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape =
                    RoundedCornerShape(15.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = SURFACE2
                    )
            ) {
                Text(
                    text = "پایان انتخاب",
                    color = WHITE
                )
            }
        }
    }
}

@Composable
fun HiddenAppItem(
    app: InstalledApp,
    onLaunch: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(
                    RoundedCornerShape(18.dp)
                )
                .background(SURFACE2)
                .clickable {
                    onLaunch()
                },
            contentAlignment =
                Alignment.Center
        ) {
            Image(
                bitmap = app.icon
                    .toBitmap(128, 128)
                    .asImageBitmap(),
                contentDescription =
                    app.label,
                modifier =
                    Modifier.size(52.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = app.label,
            color = WHITE,
            fontSize = 9.sp,
            maxLines = 1
        )

        TextButton(
            onClick = onRemove,
            contentPadding =
                PaddingValues(0.dp)
        ) {
            Text(
                text = "نمایش",
                color = ORANGE,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun SelectableAppItem(
    app: InstalledApp,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
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
                    if (selected)
                        ORANGE
                    else
                        SURFACE2
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Image(
                bitmap = app.icon
                    .toBitmap(128, 128)
                    .asImageBitmap(),
                contentDescription =
                    app.label,
                modifier =
                    Modifier.size(52.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = app.label,
            color = WHITE,
            fontSize = 9.sp,
            maxLines = 1
        )

        Text(
            text =
                if (selected)
                    "✓ انتخاب شد"
                else
                    "انتخاب",
            color =
                if (selected)
                    ORANGE
                else
                    WHITE,
            fontSize = 10.sp
        )
    }
}

fun loadHiddenApps(
    prefs: SharedPreferences
): MutableSet<String> {
    return (
        prefs.getStringSet(
            "hidden_apps",
            emptySet()
        ) ?: emptySet()
    ).toMutableSet()
}

fun saveHiddenApps(
    prefs: SharedPreferences,
    hidden: Set<String>
) {
    prefs.edit()
        .putStringSet(
            "hidden_apps",
            hidden
        )
        .apply()
}

fun hideApp(
    prefs: SharedPreferences,
    packageName: String
) {
    val set =
        loadHiddenApps(prefs)

    set.add(packageName)

    saveHiddenApps(
        prefs,
        set
    )
}

fun showApp(
    prefs: SharedPreferences,
    packageName: String
) {
    val set =
        loadHiddenApps(prefs)

    set.remove(packageName)

    saveHiddenApps(
        prefs,
        set
    )
}

fun isHidden(
    prefs: SharedPreferences,
    packageName: String
): Boolean {
    return loadHiddenApps(
        prefs
    ).contains(packageName)
}

@Composable
fun PasswordField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange =
            onValueChange,
        modifier =
            Modifier.fillMaxWidth(),
        singleLine = true,
        label = {
            Text(label)
        },
        visualTransformation =
            PasswordVisualTransformation()
    )
}

@Composable
fun OrangeButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(15.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = ORANGE,
                contentColor = WHITE
            )
    ) {
        Text(
            text = text,
            color = WHITE,
            fontSize = 16.sp
        )
    }
}

@Composable
fun DarkBackButton(
    text: String = "بازگشت",
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick
    ) {
        Text(
            text = text,
            color = WHITE
        )
    }
}

fun getLaunchableApps(
    activity: MainActivity
): List<InstalledApp> {

    val pm =
        activity.packageManager

    return pm
        .getInstalledApplications(
            PackageManager.GET_META_DATA
        )
        .filter {
            it.packageName !=
                activity.packageName &&
                pm.getLaunchIntentForPackage(
                    it.packageName
                ) != null
        }
        .map {
            InstalledApp(
                packageName =
                    it.packageName,
                label =
                    pm.getApplicationLabel(
                        it
                    ).toString(),
                icon =
                    pm.getApplicationIcon(
                        it.packageName
                    )
            )
        }
        .sortedBy {
            it.label.lowercase(
                Locale.getDefault()
            )
        }
}

fun resetDarkCalc(
    prefs: SharedPreferences
) {
    prefs.edit()
        .clear()
        .putString(
            "password",
            DEFAULT_PASSWORD
        )
        .putStringSet(
            "hidden_apps",
            emptySet()
        )
        .apply()
}

fun getSavedPassword(
    prefs: SharedPreferences
): String {
    return prefs.getString(
        "password",
        DEFAULT_PASSWORD
    ) ?: DEFAULT_PASSWORD
}

fun savePassword(
    prefs: SharedPreferences,
    password: String
) {
    prefs.edit()
        .putString(
            "password",
            password
        )
        .apply()
}

fun clearHiddenApps(
    prefs: SharedPreferences
) {
    prefs.edit()
        .putStringSet(
            "hidden_apps",
            emptySet()
        )
        .apply()
}