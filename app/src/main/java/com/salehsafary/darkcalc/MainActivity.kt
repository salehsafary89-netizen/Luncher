package com.salehsafary.darkcalc

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val SECRET_CODE = "2580"
private const val DEFAULT_PASSWORD = "123456"
private const val SECURITY_QUESTION = "نام اولین مدرسه شما چه بود؟"

data class InstalledApp(
    val label: String,
    val packageName: String,
    val icon: Bitmap,
    val isCalculator: Boolean = false
)

enum class Screen {
    HOME,
    DRAWER,
    CALCULATOR,
    LOGIN,
    RESET,
    HIDDEN
}

class MainActivity : ComponentActivity() {

    private var allApps: List<InstalledApp> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        allApps = loadInstalledApps()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    DarkCalcApp(this)
                }
            }
        }
    }

    private fun loadInstalledApps(): List<InstalledApp> {
        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = pm.queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        )

        return resolved
            .mapNotNull { info ->

                val packageName =
                    info.activityInfo.packageName

                if (packageName == packageName()) {
                    return@mapNotNull null
                }

                val label =
                    info.loadLabel(pm)?.toString()
                        ?: return@mapNotNull null

                val drawable =
                    info.loadIcon(pm)

                InstalledApp(
                    label = label,
                    packageName = packageName,
                    icon = drawableToBitmap(drawable)
                )
            }
            .distinctBy {
                it.packageName
            }
            .sortedBy {
                it.label.lowercase()
            }
    }

    private fun packageName(): String {
        return applicationContext.packageName
    }

    private fun drawableToBitmap(
        drawable: Drawable
    ): Bitmap {

        val width =
            if (drawable.intrinsicWidth > 0)
                drawable.intrinsicWidth
            else 96

        val height =
            if (drawable.intrinsicHeight > 0)
                drawable.intrinsicHeight
            else 96

        val bitmap =
            Bitmap.createBitmap(
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

    fun launchApp(packageName: String) {

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

        val prefs =
            getSharedPreferences(
                "darkcalc",
                Context.MODE_PRIVATE
            )

        val savedPassword =
            prefs.getString(
                "password",
                DEFAULT_PASSWORD
            )

        return username.isNotBlank() &&
                password == savedPassword
    }

    fun checkSecurityAnswer(
        answer: String
    ): Boolean {

        val prefs =
            getSharedPreferences(
                "darkcalc",
                Context.MODE_PRIVATE
            )

        val savedAnswer =
            prefs.getString(
                "security_answer",
                ""
            )

        return answer.trim() == savedAnswer
    }

    fun changePassword(
        password: String
    ) {

        getSharedPreferences(
            "darkcalc",
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                "password",
                password
            )
            .apply()
    }
}

@Composable
fun DarkCalcApp(
    activity: MainActivity
) {

    var screen by remember {
        mutableStateOf(Screen.HOME)
    }

    val calculator =
        InstalledApp(
            label = "ماشین حساب",
            packageName = "darkcalc.calculator",
            icon = createCalculatorBitmap(),
            isCalculator = true
        )

    val apps =
        remember {
            activity
                .let {
                    getAppsWithoutHidden(it)
                }
        }

    val hiddenApps =
        remember {
            emptyList<InstalledApp>()
        }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {

            if (targetState.ordinal >
                initialState.ordinal
            ) {

                (
                    slideInHorizontally(
                        animationSpec =
                            tween(280),
                        initialOffsetX = {
                            it
                        }
                    ) + fadeIn(
                        animationSpec =
                            tween(280)
                    )
                ).togetherWith(
                    slideOutHorizontally(
                        animationSpec =
                            tween(280),
                        targetOffsetX = {
                            -it / 3
                        }
                    ) + fadeOut(
                        animationSpec =
                            tween(180)
                    )
                )

            } else {

                (
                    slideInHorizontally(
                        animationSpec =
                            tween(280),
                        initialOffsetX = {
                            -it
                        }
                    ) + fadeIn(
                        animationSpec =
                            tween(280)
                    )
                ).togetherWith(
                    slideOutHorizontally(
                        animationSpec =
                            tween(280),
                        targetOffsetX = {
                            it / 3
                        }
                    ) + fadeOut(
                        animationSpec =
                            tween(180)
                    )
                )
            }

        },
        label = "screen_transition"
    ) { currentScreen ->

        when (currentScreen) {

            Screen.HOME -> {

                HomeScreen(
                    apps = apps,
                    onOpenDrawer = {
                        screen = Screen.DRAWER
                    },
                    onOpenLogin = {
                        screen = Screen.LOGIN
                    }
                )
            }

            Screen.DRAWER -> {

                AppDrawer(
                    apps = apps,
                    calculator = calculator,
                    onLaunch = {
                        activity.launchApp(it)
                    },
                    onCalculator = {
                        screen = Screen.CALCULATOR
                    },
                    onBack = {
                        screen = Screen.HOME
                    }
                )
            }

            Screen.CALCULATOR -> {

                CalculatorScreen(
                    onBack = {
                        screen = Screen.DRAWER
                    },
                    onSecretCode = {
                        screen = Screen.LOGIN
                    }
                )
            }

            Screen.LOGIN -> {

                LoginScreen(
                    activity = activity,
                    onBack = {
                        screen = Screen.CALCULATOR
                    },
                    onSuccess = {
                        screen = Screen.HIDDEN
                    },
                    onReset = {
                        screen = Screen.RESET
                    }
                )
            }

            Screen.RESET -> {

                ResetPasswordScreen(
                    activity = activity,
                    onBack = {
                        screen = Screen.LOGIN
                    },
                    onDone = {
                        screen = Screen.LOGIN
                    }
                )
            }

            Screen.HIDDEN -> {

                HiddenAppsScreen(
                    apps = hiddenApps,
                    onLaunch = {
                        activity.launchApp(it)
                    },
                    onBack = {
                        screen = Screen.HOME
                    }
                )
            }
        }
    }
}

fun getAppsWithoutHidden(
    activity: MainActivity
): List<InstalledApp> {

    val pm = activity.packageManager

    val intent =
        Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

    return pm.queryIntentActivities(
        intent,
        PackageManager.MATCH_ALL
    )
        .mapNotNull { info ->

            val packageName =
                info.activityInfo.packageName

            if (
                packageName ==
                activity.packageName
            ) {
                return@mapNotNull null
            }

            val label =
                info.loadLabel(pm)?.toString()
                    ?: return@mapNotNull null

            val drawable =
                info.loadIcon(pm)

            InstalledApp(
                label = label,
                packageName = packageName,
                icon = drawableToBitmapStatic(
                    drawable
                )
            )
        }
        .distinctBy {
            it.packageName
        }
        .sortedBy {
            it.label.lowercase()
        )
}

fun drawableToBitmapStatic(
    drawable: Drawable
): Bitmap {

    val width =
        if (drawable.intrinsicWidth > 0)
            drawable.intrinsicWidth
        else 96

    val height =
        if (drawable.intrinsicHeight > 0)
            drawable.intrinsicHeight
        else 96

    val bitmap =
        Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

    val canvas = Canvas(bitmap)

    drawable.setBounds(
        0,
        0,
        width,
        height
    )

    drawable.draw(canvas)

    return bitmap
}

fun createCalculatorBitmap(): Bitmap {

    val bitmap =
        Bitmap.createBitmap(
            96,
            96,
            Bitmap.Config.ARGB_8888
        )

    val canvas = Canvas(bitmap)

    val paint =
        android.graphics.Paint(
            android.graphics.Paint.ANTI_ALIAS_FLAG
        )

    paint.color =
        android.graphics.Color.rgb(
            35,
            35,
            35
        )

    canvas.drawRoundRect(
        4f,
        4f,
        92f,
        92f,
        18f,
        18f,
        paint
    )

    paint.color =
        android.graphics.Color.WHITE

    paint.textSize = 54f
    paint.textAlign =
        android.graphics.Paint.Align.CENTER

    canvas.drawText(
        "÷",
        48f,
        67f,
        paint
    )

    return bitmap
}

@Composable
fun HomeScreen(
    apps: List<InstalledApp>,
    onOpenDrawer: () -> Unit,
    onOpenLogin: () -> Unit
) {

    var page by remember {
        mutableStateOf(0)
    }

    val pageSize = 12

    val pages =
        if (apps.isEmpty()) {
            listOf(emptyList())
        } else {
            apps.chunked(pageSize)
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 18.dp,
                vertical = 22.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column {

                LiveClock()

                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )

                LiveDate()
            }

            TextButton(
                onClick = onOpenLogin
            ) {
                Text(
                    text = "●",
                    fontSize = 22.sp
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            if (
                pages.isNotEmpty() &&
                page < pages.size
            ) {

                LazyVerticalGrid(
                    columns =
                        GridCells.Fixed(4),
                    modifier =
                        Modifier.fillMaxSize(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(22.dp)
                ) {

                    items(
                        items = pages[page],
                        key = {
                            it.packageName
                        }
                    ) { app ->

                        AppIcon(
                            app = app,
                            onClick = {
                            }
                        )
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
                Modifier.height(12.dp)
        )

        Dock(
            onDrawer = onOpenDrawer
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
            bitmap =
                app.icon.asImageBitmap(),
            contentDescription =
                app.label,
            modifier =
                Modifier
                    .size(58.dp)
                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
                    )
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

@Composable
fun AppDrawer(
    apps: List<InstalledApp>,
    calculator: InstalledApp,
    onLaunch: (String) -> Unit,
    onCalculator: () -> Unit,
    onBack: () -> Unit
) {

    val drawerApps =
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
            horizontalArrangement =
                Arrangement.spacedBy(8.dp),
            verticalArrangement =
                Arrangement.spacedBy(20.dp)
        ) {

            items(
                items = drawerApps,
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
                display += value
            }
        }
    }

    val rows =
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
        modifier = Modifier
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
                fontSize = 26.sp,
                fontWeight =
                    FontWeight.Bold
            )

            OutlinedButton(
                onClick = onBack
            ) {
                Text("بازگشت")
            }
        }

        Spacer(
            modifier =
                Modifier.height(30.dp)
        )

        Text(
            text =
                if (display.isEmpty()) {
                    "0"
                } else {
                    display
                },
            fontSize = 38.sp,
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
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
                modifier =
                    Modifier.height(8.dp)
            )
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
            listOf(
                '+',
                '-',
                '*',
                '/'
            ).firstOrNull {
                normalized.contains(it)
            }
                ?: return normalized

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

                '+' ->
                    first + second

                '-' ->
                    first - second

                '*' ->
                    first * second

                '/' -> {

                    if (second == 0.0) {
                        return "خطا"
                    }

                    first / second
                }

                else ->
                    return "خطا"
            }

        when {

            !result.isFinite() ->
                "خطا"

            result % 1.0 == 0.0 ->
                result
                    .toLong()
                    .toString()

            else ->
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
                text = "ورود",
                fontSize = 30.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(32.dp)
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
                    RoundedCornerShape(
                        12.dp
                    )
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
            text = SECURITY_QUESTION,
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

            Text(
                "ذخیره رمز جدید"
            )
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