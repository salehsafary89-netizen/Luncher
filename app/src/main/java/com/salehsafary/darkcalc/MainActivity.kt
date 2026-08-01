package com.salehsafary.darkcalc

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val USERNAME = "Saleh Safari"
private const val DEFAULT_PASSWORD = "123456789"
private const val SECURITY_ANSWER = "آقای سعیدی"
private const val SECRET_CODE = "1234"

data class InstalledApp(
    val label: String,
    val packageName: String,
    val icon: Bitmap,
    val isCalculator: Boolean = false
)

class MainActivity : ComponentActivity() {

    private var currentPassword by mutableStateOf(DEFAULT_PASSWORD)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DarkCalcLauncher(this)
        }
    }

    fun checkLogin(
        username: String,
        password: String
    ): Boolean {
        return username.equals(USERNAME, ignoreCase = true) &&
                password == currentPassword
    }

    fun checkSecurityAnswer(
        answer: String
    ): Boolean {
        return answer.trim() == SECURITY_ANSWER
    }

    fun changePassword(
        password: String
    ) {
        currentPassword = password
    }

    fun launchApp(
        packageName: String
    ) {
        try {
            val intent =
                packageManager.getLaunchIntentForPackage(packageName)

            if (intent != null) {
                startActivity(intent)
            }
        } catch (_: Exception) {
        }
    }

    fun getInstalledApps(): List<InstalledApp> {

        val pm = packageManager

        val apps = mutableListOf<InstalledApp>()

        val installed =
            pm.getInstalledApplications(
                PackageManager.GET_META_DATA
            )

        for (app in installed) {

            if (app.packageName == packageName) {
                continue
            }

            if (
                pm.getLaunchIntentForPackage(
                    app.packageName
                ) == null
            ) {
                continue
            }

            val label =
                pm.getApplicationLabel(app).toString()

            val icon =
                drawableToBitmap(
                    pm.getApplicationIcon(app)
                )

            apps.add(
                InstalledApp(
                    label = label,
                    packageName = app.packageName,
                    icon = icon
                )
            )
        }

        apps.sortBy {
            it.label.lowercase()
        }

        val calculatorIcon =
            createCalculatorIcon()

        apps.add(
            0,
            InstalledApp(
                label = "Tools",
                packageName = "darkcalc.calculator",
                icon = calculatorIcon,
                isCalculator = true
            )
        )

        return apps
    }

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

    private fun createCalculatorIcon(): Bitmap {

        val bitmap =
            Bitmap.createBitmap(
                128,
                128,
                Bitmap.Config.ARGB_8888
            )

        val canvas =
            Canvas(bitmap)

        canvas.drawColor(
            android.graphics.Color.rgb(
                72,
                76,
                82
            )
        )

        return bitmap
    }

}

@Composable
fun DarkCalcLauncher(
    activity: MainActivity
) {

    MaterialTheme(
        colorScheme =
            darkColorScheme(
                background =
                    Color(0xFF101114),

                surface =
                    Color(0xFF191B20),

                primary =
                    Color(0xFF4F8CFF)
            )
    ) {

        var screen by remember {
            mutableStateOf("home")
        }

        var direction by remember {
            mutableStateOf(1)
        }

        val apps =
            remember {
                activity.getInstalledApps()
            }

        fun navigate(
            target: String
        ) {

            direction =
                if (target == "home")
                    -1
                else
                    1

            screen = target
        }

        AnimatedContent(
            targetState = screen,

            transitionSpec = {

                if (direction > 0) {

                    (
                        slideInHorizontally {
                            it
                        } + fadeIn()
                    ) togetherWith (
                        slideOutHorizontally {
                            -it / 3
                        } + fadeOut()
                    )

                } else {

                    (
                        slideInHorizontally {
                            -it
                        } + fadeIn()
                    ) togetherWith (
                        slideOutHorizontally {
                            it / 3
                        } + fadeOut()
                    )
                }
            },

            label = "screen_motion"
        ) { currentScreen ->

            when (currentScreen) {

                "home" -> {

                    HomeScreen(
                        apps = apps,

                        onLaunch = {
                            activity.launchApp(it)
                        },

                        onCalculator = {
                            navigate("calculator")
                        }
                    )
                }

                "calculator" -> {

                    CalculatorScreen(
                        onBack = {
                            navigate("home")
                        },

                        onSecretCode = {
                            navigate("login")
                        }
                    )
                }

                "login" -> {

                    LoginScreen(
                        activity = activity,

                        onBack = {
                            navigate("calculator")
                        },

                        onSuccess = {
                            navigate("hidden")
                        },

                        onReset = {
                            navigate("reset")
                        }
                    )
                }

                "reset" -> {

                    ResetPasswordScreen(
                        activity = activity,

                        onBack = {
                            navigate("login")
                        },

                        onDone = {
                            navigate("login")
                        }
                    )
                }

                "hidden" -> {

                    HiddenAppsScreen(
                        apps =
                            apps.filter {
                                !it.isCalculator
                            },

                        onLaunch = {
                            activity.launchApp(it)
                        },

                        onBack = {
                            navigate("home")
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

    val pages =
        remember(apps) {
            apps.chunked(20)
        }

    val pageCount =
        maxOf(
            1,
            pages.size
        )

    val pagerState =
        rememberPagerState(
            pageCount = {
                pageCount
            }
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                )
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column {

                LiveClock()

                LiveDate()
            }

            Text(
                text = "Home",
                fontSize = 18.sp,
                fontWeight =
                    FontWeight.Medium
            )
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        HorizontalPager(
            state = pagerState,

            modifier =
                Modifier.weight(1f),

            pageSpacing = 8.dp
        ) { page ->

            val pageApps =
                pages
                    .getOrNull(page)
                    .orEmpty()

            LazyVerticalGrid(
                columns =
                    GridCells.Fixed(4),

                modifier =
                    Modifier.fillMaxSize(),

                contentPadding =
                    PaddingValues(6.dp),

                horizontalArrangement =
                    Arrangement.spacedBy(10.dp),

                verticalArrangement =
                    Arrangement.spacedBy(18.dp)
            ) {

                items(
                    items = pageApps,

                    key = {
                        it.packageName
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

        PageIndicator(
            count = pageCount,
            current =
                pagerState.currentPage
        )
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
                },

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Box(
            modifier =
                Modifier
                    .size(62.dp)
                    .clip(
                        RoundedCornerShape(
                            18.dp
                        )
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surface
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Image(
                bitmap =
                    app.icon
                        .asImageBitmap(),

                contentDescription =
                    app.label,

                modifier =
                    Modifier.size(52.dp),

                contentScale =
                    ContentScale.Fit
            )
        }

        Spacer(
            modifier =
                Modifier.height(5.dp)
        )

        Text(
            text = app.label,

            fontSize = 10.sp,

            maxLines = 1
        )
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
                    if (index == current)
                        "●"
                    else
                        "○",

                fontSize = 10.sp,

                modifier =
                    Modifier.padding(
                        horizontal = 3.dp
                    )
            )
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

            time =
                SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(
                    Date()
                )

            delay(1000)
        }
    }

    Text(
        text = time,

        fontSize = 34.sp,

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
                ).format(
                    Date()
                )

            delay(60000)
        }
    }

    Text(
        text = date,

        fontSize = 12.sp
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

    fun press(
        value: String
    ) {

        when (value) {

            "C" -> {
                display = ""
            }

            "=" -> {

                if (
                    display == SECRET_CODE
                ) {

                    display = ""

                    onSecretCode()

                } else {

                    display =
                        calculateSimple(
                            display
                        )
                }
            }

            else -> {

                if (
                    display.length < 24
                ) {

                    display += value
                }
            }
        }
    }

    val rows =
        listOf(
            listOf(
                "7",
                "8",
                "9",
                "÷"
            ),

            listOf(
                "4",
                "5",
                "6",
                "×"
            ),

            listOf(
                "1",
                "2",
                "3",
                "−"
            ),

            listOf(
                "0",
                "C",
                "=",
                "+"
            )
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(18.dp),

        verticalArrangement =
            Arrangement.Center
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text = "Calculator",

                fontSize = 25.sp,

                fontWeight =
                    FontWeight.Bold
            )

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "Back"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(14.dp)
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surface,

                        RoundedCornerShape(
                            20.dp
                        )
                    )
                    .padding(20.dp),

            contentAlignment =
                Alignment.BottomEnd
        ) {

            Text(
                text =
                    if (
                        display.isEmpty()
                    )
                        "0"
                    else
                        display,

                fontSize = 40.sp,

                maxLines = 1
            )
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Column(
            modifier =
                Modifier.weight(2f),

            verticalArrangement =
                Arrangement.spacedBy(
                    9.dp
                )
        ) {

            rows.forEach { row ->

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            9.dp
                        )
                ) {

                    row.forEach { value ->

                        Button(
                            onClick = {
                                press(value)
                            },

                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),

                            shape =
                                RoundedCornerShape(
                                    18.dp
                                )
                        ) {

                            Text(
                                text = value,

                                fontSize = 23.sp
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

    if (
        expression.isBlank()
    ) {
        return "0"
    }

    return try {

        val normalized =
            expression
                .replace(
                    "×",
                    "*"
                )
                .replace(
                    "÷",
                    "/"
                )
                .replace(
                    "−",
                    "-"
                )

        val operator =
            listOf(
                '+',
                '-',
                '*',
                '/'
            ).firstOrNull {

                normalized.contains(
                    it
                )
            }
                ?: return normalized

        val parts =
            normalized.split(
                operator
            )

        if (
            parts.size != 2
        ) {
            return "Error"
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

                '+' ->
                    first + second

                '-' ->
                    first - second

                '*' ->
                    first * second

                '/' -> {

                    if (
                        second == 0.0
                    ) {
                        return "Error"
                    }

                    first / second
                }

                else ->
                    return "Error"
            }

        if (
            !result.isFinite()
        ) {

            "Error"

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

        "Error"
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
                    .align(
                        Alignment.Center
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "Login",

                fontSize = 30.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp)
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
                        text = "Username"
                    )
                },

                placeholder = {
                    Text(
                        text = "Saleh Safari"
                    )
                }
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
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
                        text = "Password"
                    )
                },

                visualTransformation =
                    PasswordVisualTransformation()
            )

            Spacer(
                modifier =
                    Modifier.height(18.dp)
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
                            "Username or password is incorrect"
                    }
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                shape =
                    RoundedCornerShape(
                        14.dp
                    )
            ) {

                Text(
                    text = "Login",

                    fontSize = 17.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            TextButton(
                onClick = onReset
            ) {

                Text(
                    text =
                        "Forgot password?"
                )
            }

            if (
                error.isNotEmpty()
            ) {

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
        ) {

            Text(
                text = "Back"
            )
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

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "Forgot password",

            fontSize = 27.sp,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Text(
            text =
                "معلم مورد علاقه‌ات کی بود؟",

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
                Text(
                    text = "پاسخ"
                )
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
                Text(
                    text = "رمز جدید"
                )
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
                Text(
                    text = "تکرار رمز"
                )
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
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(
                    14.dp
                )
        ) {

            Text(
                text =
                    "ذخیره رمز جدید"
            )
        }

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        TextButton(
            onClick = onBack
        ) {

            Text(
                text = "بازگشت"
            )
        }

        if (
            message.isNotEmpty()
        ) {

            Spacer(
                modifier =
                    Modifier.height(8.dp)
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
                text = "Private Apps",

                fontSize = 25.sp,

                fontWeight =
                    FontWeight.Bold
            )

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "Home"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        if (
            apps.isEmpty()
        ) {

            Box(
                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        "No apps found"
                )
            }

        } else {

            LazyVerticalGrid(
                columns =
                    GridCells.Fixed(4),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        18.dp
                    )
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
 * این قسمت عمداً خالی نیست.
 * برای اینکه فایل نهایی بدون فاصله و بدون خطای اتصال باشد،
 * ادامه‌ی کد از اینجا به بعد باید دقیقاً پشت قسمت ۵ قرار بگیرد.
 */

@Composable
fun LauncherInfo() {

    Spacer(
        modifier =
            Modifier.height(1.dp)
    )
}

/*
 * تنظیمات ظاهری سبک لانچر
 */

@Composable
fun LightweightStyle() {

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Color.Transparent
                )
    )
}

/*
 * کنترل ساده‌ی صفحه‌های لانچر.
 * HorizontalPager صفحات Home را به صورت افقی
 * و با حرکت نرم جابه‌جا می‌کند.
 */

@Composable
fun LauncherPageMotion(
    pageCount: Int,
    content: @Composable (Int) -> Unit
) {

    val state =
        rememberPagerState(
            pageCount = {
                maxOf(
                    1,
                    pageCount
                )
            }
        )

    HorizontalPager(
        state = state,

        modifier =
            Modifier.fillMaxSize(),

        pageSpacing = 8.dp
    ) { page ->

        content(page)
    }
}

/*
 * نقطه‌ی ورود ظاهری همیشه Home است.
 * هیچ App Drawer یا Dock در این نسخه وجود ندارد.
 */

@Composable
fun DefaultLauncherHome(
    apps: List<InstalledApp>,
    onLaunch: (String) -> Unit,
    onCalculator: () -> Unit
) {

    HomeScreen(
        apps = apps,

        onLaunch = onLaunch,

        onCalculator = onCalculator
    )
}

/*
 * پایان فایل.
 *
 * ساختار نهایی:
 *
 * باز شدن برنامه
 *       ↓
 * Home Screen
 *       ↓
 * همه‌ی برنامه‌ها در Home
 *       ↓
 * Calculator به عنوان یک آیکن عادی
 *       ↓
 * وارد کردن 1234 و =
 *       ↓
 * Login
 *       ↓
 * Saleh Safari
 *       ↓
 * 123456789
 *       ↓
 * Private Apps
 *
 * Forgot password:
 *       ↓
 * معلم مورد علاقه‌ات کی بود؟
 *       ↓
 * آقای سعیدی
 *       ↓
 * رمز جدید
 *
 * هیچ App Drawer و هیچ Calculator Dock وجود ندارد.
 */

