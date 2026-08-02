package com.salehsafary.darkcalc

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
private const val USERNAME = "Saleh Safari"
private const val DEFAULT_PASSWORD = "123456789"
private const val SECURITY_ANSWER = "آقای ساعدی"

private val BG = Color.Black
private val CARD = Color(0xFF1C1C1C)
private val ORANGE = Color(0xFFFF9800)
private val WHITE = Color.White

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    private val apps =
        mutableStateListOf<InstalledApp>()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
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
                        it.packageName,
                        pm.getApplicationLabel(it)
                            .toString(),
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

        apps.clear()
        apps.addAll(result)
    }

    private fun launchApp(
        packageName: String
    ) {
        packageManager
            .getLaunchIntentForPackage(
                packageName
            )
            ?.let {
                startActivity(it)
            }
    }
}

val LocalMainActivity =
    staticCompositionLocalOf<MainActivity> {
        error("MainActivity not available")
    }

@Composable
fun Home(
    apps: List<AppInfo>,
    prefs: SharedPreferences,
    onCalc: () -> Unit,
    onLaunch: (String) -> Unit
) {
    val hidden = remember {
        mutableStateOf(
            prefs.getStringSet(
                "hidden",
                emptySet()
            ) ?: emptySet()
        )
    }

    val visible = remember(
        apps,
        hidden.value
    ) {
        apps.filter {
            !hidden.value.contains(
                it.packageName
            )
        }
    }

    CompositionLocalProvider(
        LocalMainActivity provides
            LocalMainActivity.current
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BG)
                .padding(10.dp)
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
                Clock()

                Spacer(
                    Modifier.height(8.dp)
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
                    Arrangement.spacedBy(16.dp),
                contentPadding =
                    PaddingValues(
                        bottom = 16.dp
                    )
            ) {

                item(
                    key = "calculator"
                ) {
                    CalculatorIcon(onCalc)
                }

                items(
                    visible,
                    key = {
                        it.packageName
                    }
                ) {
                    AppItem(
                        app = it,
                        onClick = {
                            onLaunch(
                                it.packageName
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Clock() {
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
            .background(PANEL),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                "◷",
                color = WHITE,
                fontSize = 48.sp
            )

            Text(
                time,
                color = ORANGE,
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
        date,
        color = WHITE,
        fontSize = 14.sp
    )
}

@Composable
fun AppItem(
    app: AppInfo,
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
                .background(PANEL2),
            contentAlignment =
                Alignment.Center
        ) {
            Image(
                bitmap = app.icon
                    .toBitmap(128, 128)
                    .asImageBitmap(),
                contentDescription =
                    app.name,
                modifier =
                    Modifier.size(52.dp)
            )
        }

        Text(
            app.name,
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
                    "+ × ÷",
                    color = WHITE,
                    fontSize = 11.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    "▰",
                    color = WHITE,
                    fontSize = 22.sp
                )
            }
        }

        Text(
            "ماشین حساب",
            color = WHITE,
            fontSize = 9.sp
        )
    }
}

@Composable
fun Calculator(
    onBack: () -> Unit,
    onCode: () -> Unit
) {
    var text by remember {
        mutableStateOf("")
    }

    val keys = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "−"),
        listOf("0", "C", "=", "+")
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(BG)
            .padding(12.dp)
    ) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Text(
                "ماشین حساب",
                color = WHITE,
                fontSize = 24.sp,
                fontWeight =
                    FontWeight.Bold
            )

            TextButton(onClick = onBack) {
                Text(
                    "بازگشت",
                    color = WHITE
                )
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment =
                Alignment.BottomEnd
        ) {
            Text(
                if (text.isEmpty())
                    "0"
                else
                    text,
                color = WHITE,
                fontSize = 42.sp
            )
        }

        Column(
            Modifier.weight(2f)
        ) {
            keys.forEach { row ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    row.forEach { key ->
                        CalcKey(
                            key,
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(3.dp)
                        ) {
                            when (key) {
                                "C" -> text = ""

                                "=" -> {
                                    if (
                                        text ==
                                        CALC_CODE
                                    ) {
                                        text = ""
                                        onCode()
                                    } else {
                                        text =
                                            simpleCalc(
                                                text
                                            )
                                    }
                                }

                                else -> {
                                    text += key
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
fun CalcKey(
    key: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape =
            RoundedCornerShape(18.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    if (
                        key in listOf(
                            "+",
                            "−",
                            "×",
                            "÷"
                        )
                    )
                        ORANGE
                    else
                        PANEL2
            )
    ) {
        Text(
            key,
            color = WHITE,
            fontSize = 21.sp
        )
    }
}

fun simpleCalc(
    value: String
): String {
    if (value.isBlank()) return "0"

    return try {
        val v = value
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
                v.contains(it)
            } ?: return v

        val p = v.split(op)

        if (p.size != 2) {
            return "خطا"
        }

        val a =
            p[0].trim().toDouble()

        val b =
            p[1].trim().toDouble()

        val result =
            when (op) {
                '+' -> a + b
                '-' -> a - b
                '*' -> a * b
                '/' -> {
                    if (b == 0.0)
                        return "خطا"
                    a / b
                }

                else -> return "خطا"
            }

        if (
            result.isFinite() &&
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
fun Login(
    password: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onReset: () -> Unit
) {
    var user by remember {
        mutableStateOf("")
    }

    var pass by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf("")
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(BG)
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Text(
            "ورود",
            color = WHITE,
            fontSize = 30.sp,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            Modifier.height(22.dp)
        )

        OutlinedTextField(
            value = user,
            onValueChange = {
                user = it
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
            Modifier.height(10.dp)
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
            Modifier.height(14.dp)
        )

        Button(
            onClick = {
                if (
                    user.trim() == USERNAME &&
                    pass == password
                ) {
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
                "ورود",
                color = WHITE
            )
        }

        TextButton(
            onClick = onReset
        ) {
            Text(
                "فراموشی رمز عبور",
                color = WHITE
            )
        }

        if (error.isNotEmpty()) {
            Text(
                error,
                color = ORANGE
            )
        }

        TextButton(
            onClick = onBack
        ) {
            Text(
                "بازگشت",
                color = WHITE
            )
        }
    }
}

@Composable
fun ResetPassword(
    onBack: () -> Unit,
    onDone: (String) -> Unit
) {
    var answer by remember {
        mutableStateOf("")
    }

    var newPass by remember {
        mutableStateOf("")
    }

    var confirm by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(BG)
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Text(
            "بازنشانی رمز",
            color = WHITE,
            fontSize = 27.sp,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            Modifier.height(16.dp)
        )

        Text(
            "معلم مورد علاقه‌ات کی بود؟",
            color = WHITE
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
            Modifier.height(8.dp)
        )

        OutlinedTextField(
            value = newPass,
            onValueChange = {
                newPass = it
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
            Modifier.height(8.dp)
        )

        OutlinedTextField(
            value = confirm,
            onValueChange = {
                confirm = it
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
            Modifier.height(12.dp)
        )

        Button(
            onClick = {
                when {
                    answer.trim() != ANSWER ->
                        message =
                            "پاسخ اشتباه است"

                    newPass.length < 6 ->
                        message =
                            "رمز خیلی کوتاه است"

                    newPass != confirm ->
                        message =
                            "رمزها یکسان نیستند"

                    else ->
                        onDone(newPass)
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
                "ذخیره",
                color = WHITE
            )
        }

        TextButton(
            onClick = onBack
        ) {
            Text(
                "بازگشت",
                color = WHITE
            )
        }

        if (message.isNotEmpty()) {
            Text(
                message,
                color = ORANGE
            )
        }
    }
}

@Composable
fun HiddenApps(
    apps: List<AppInfo>,
    prefs: SharedPreferences,
    onExit: () -> Unit,
    onLaunch: (String) -> Unit
) {
    var hidden by remember {
        mutableStateOf(
            prefs.getStringSet(
                "hidden",
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
        Modifier
            .fillMaxSize()
            .background(BG)
            .padding(16.dp)
    ) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
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
                onClick = onExit
            ) {
                Text(
                    "خروج",
                    color = WHITE
                )
            }
        }

        Spacer(
            Modifier.height(8.dp)
        )

        if (!selecting) {

            Text(
                if (hiddenApps.isEmpty())
                    "برنامه‌ای مخفی نشده"
                else
                    "برنامه‌های مخفی‌شده",
                color = ORANGE
            )

            Spacer(
                Modifier.height(8.dp)
            )

            if (hiddenApps.isEmpty()) {

                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        "برای انتخاب برنامه\n" +
                        "دکمه پایین را بزن",
                        color = WHITE
                    )
                }

            } else {

                LazyVerticalGrid(
                    columns =
                        GridCells.Fixed(4),
                    modifier =
                        Modifier.weight(1f),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp),
                    contentPadding =
                        PaddingValues(
                            bottom = 10.dp
                        )
                ) {
                    items(
                        hiddenApps,
                        key = {
                            it.packageName
                        }
                    ) { app ->
                        HiddenItem(
                            app = app,
                            onLaunch = {
                                onLaunch(
                                    app.packageName
                                )
                            },
                            onShow = {

                                val set =
                                    hidden.toMutableSet()

                                set.remove(
                                    app.packageName
                                )

                                hidden = set

                                prefs.edit()
                                    .putStringSet(
                                        "hidden",
                                        set
                                    )
                                    .apply()
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    selecting = true
                },
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(15.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = ORANGE
                    )
            ) {
                Text(
                    "برنامه‌های بیشتر…",
                    color = WHITE
                )
            }

        } else {

            Text(
                "برنامه‌هایی را انتخاب کن",
                color = WHITE
            )

            Spacer(
                Modifier.height(8.dp)
            )

            LazyVerticalGrid(
                columns =
                    GridCells.Fixed(4),
                modifier =
                    Modifier.weight(1f),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp),
                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {
                items(
                    apps,
                    key = {
                        it.packageName
                    }
                ) { app ->

                    SelectItem(
                        app = app,
                        selected =
                            hidden.contains(
                                app.packageName
                            ),
                        onClick = {

                            val set =
                                hidden.toMutableSet()

                            if (
                                set.contains(
                                    app.packageName
                                )
                            ) {
                                set.remove(
                                    app.packageName
                                )
                            } else {
                                set.add(
                                    app.packageName
                                )
                            }

                            hidden = set

                            prefs.edit()
                                .putStringSet(
                                    "hidden",
                                    set
                                )
                                .apply()
                        }
                    )
                }
            }

            Button(
                onClick = {
                    selecting = false
                },
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(15.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = PANEL2
                    )
            ) {
                Text(
                    "پایان انتخاب",
                    color = WHITE
                )
            }
        }
    }
}

@Composable
fun HiddenItem(
    app: AppInfo,
    onLaunch: () -> Unit,
    onShow: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(
                    RoundedCornerShape(18.dp)
                )
                .background(PANEL2)
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
                    app.name,
                modifier =
                    Modifier.size(52.dp)
            )
        }

        Text(
            app.name,
            color = WHITE,
            fontSize = 9.sp,
            maxLines = 1
        )

        TextButton(
            onClick = onShow,
            contentPadding =
                PaddingValues(0.dp)
        ) {
            Text(
                "نمایش",
                color = ORANGE,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun SelectItem(
    app: AppInfo,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(
                    RoundedCornerShape(18.dp)
                )
                .background(
                    if (selected)
                        ORANGE
                    else
                        PANEL2
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Image(
                bitmap = app.icon
                    .toBitmap(128, 128)
                    .asImageBitmap(),
                contentDescription =
                    app.name,
                modifier =
                    Modifier.size(52.dp)
            )
        }

        Text(
            app.name,
            color = WHITE,
            fontSize = 9.sp,
            maxLines = 1
        )

        Text(
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

fun hiddenApps(
    prefs: SharedPreferences
): MutableSet<String> {
    return (
        prefs.getStringSet(
            "hidden",
            emptySet()
        ) ?: emptySet()
    ).toMutableSet()
}

fun saveHidden(
    prefs: SharedPreferences,
    set: Set<String>
) {
    prefs.edit()
        .putStringSet(
            "hidden",
            set
        )
        .apply()
}

fun hidePackage(
    prefs: SharedPreferences,
    packageName: String
) {
    val set =
        hiddenApps(prefs)

    set.add(packageName)

    saveHidden(
        prefs,
        set
    )
}

fun showPackage(
    prefs: SharedPreferences,
    packageName: String
) {
    val set =
        hiddenApps(prefs)

    set.remove(packageName)

    saveHidden(
        prefs,
        set
    )
}

fun savedPassword(
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

fun resetAll(
    prefs: SharedPreferences
) {
    prefs.edit()
        .clear()
        .putString(
            "password",
            DEFAULT_PASSWORD
        )
        .putStringSet(
            "hidden",
            emptySet()
        )
        .apply()
}

fun isHiddenPackage(
    prefs: SharedPreferences,
    packageName: String
): Boolean {
    return hiddenApps(
        prefs
    ).contains(packageName)
}

fun getAllLaunchableApps(
    activity: MainActivity
): List<AppInfo> {
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
            AppInfo(
                packageName =
                    it.packageName,
                name =
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
            it.name.lowercase(
                Locale.getDefault()
            )
        }
}