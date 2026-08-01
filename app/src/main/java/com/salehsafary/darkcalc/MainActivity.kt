package com.salehsafary.darkcalc

import android.content.Contextimport android.content.Intentimport android.content.pm.PackageManagerimport android.graphics.drawable.Drawableimport android.os.Bundleimport androidx.activity.ComponentActivityimport androidx.activity.compose.setContentimport androidx.compose.animation.AnimatedContentimport androidx.compose.animation.ExperimentalAnimationApiimport androidx.compose.animation.SizeTransformimport androidx.compose.animation.fadeInimport androidx.compose.animation.fadeOutimport androidx.compose.animation.slideInHorizontallyimport androidx.compose.animation.slideOutHorizontallyimport androidx.compose.animation.togetherWithimport androidx.compose.animation.withimport androidx.compose.foundation.backgroundimport androidx.compose.foundation.clickableimport androidx.compose.foundation.gestures.detectHorizontalDragGesturesimport androidx.compose.foundation.layout.Arrangementimport androidx.compose.foundation.layout.Boximport androidx.compose.foundation.layout.Columnimport androidx.compose.foundation.layout.Rowimport androidx.compose.foundation.layout.Spacerimport androidx.compose.foundation.layout.fillMaxSizeimport androidx.compose.foundation.layout.fillMaxWidthimport androidx.compose.foundation.layout.heightimport androidx.compose.foundation.layout.imePaddingimport androidx.compose.foundation.layout.paddingimport androidx.compose.foundation.layout.sizeimport androidx.compose.foundation.lazy.grid.GridCellsimport androidx.compose.foundation.lazy.grid.LazyVerticalGridimport androidx.compose.foundation.lazy.grid.itemsimport androidx.compose.material3.Buttonimport androidx.compose.material3.MaterialThemeimport androidx.compose.material3.OutlinedButtonimport androidx.compose.material3.OutlinedTextFieldimport androidx.compose.material3.Surfaceimport androidx.compose.material3.Textimport androidx.compose.material3.TextButtonimport androidx.compose.runtime.Composableimport androidx.compose.runtime.LaunchedEffectimport androidx.compose.runtime.getValueimport androidx.compose.runtime.mutableStateListOfimport androidx.compose.runtime.mutableStateOfimport androidx.compose.runtime.rememberimport androidx.compose.runtime.setValueimport androidx.compose.ui.Alignmentimport androidx.compose.ui.Modifierimport androidx.compose.ui.graphics.asImageBitmapimport androidx.compose.ui.input.pointer.pointerInputimport androidx.compose.ui.layout.ContentScaleimport androidx.compose.ui.text.font.FontWeightimport androidx.compose.ui.text.input.PasswordVisualTransformationimport androidx.compose.ui.unit.dpimport androidx.compose.ui.unit.spimport kotlinx.coroutines.delayimport java.text.SimpleDateFormatimport java.util.Dateimport java.util.Locale

private const val PREFS = "darkcalc_prefs"private const val KEY_PASSWORD = "password"private const val KEY_SECURITY = "security_answer"

private const val DEFAULT_PASSWORD = "123456"private const val SECURITY_QUESTION = "نام اولین مدرسه شما چیست؟"

private const val CALCULATOR_PACKAGE = "com.salehsafary.darkcalc.calculator"

data class InstalledApp(val packageName: String,val label: String,val icon: Drawable)

class MainActivity : ComponentActivity() {

private val hiddenPackages = mutableStateListOf<String>()

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    loadHiddenApps()

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

private fun preferences() =
    getSharedPreferences(PREFS, Context.MODE_PRIVATE)

fun checkLogin(
    username: String,
    password: String
): Boolean {
    val savedPassword =
        preferences().getString(
            KEY_PASSWORD,
            DEFAULT_PASSWORD
        ) ?: DEFAULT_PASSWORD

    return username.isNotEmpty() &&
            password == savedPassword
}

fun checkSecurityAnswer(
    answer: String
): Boolean {
    val saved =
        preferences().getString(
            KEY_SECURITY,
            ""
        ) ?: ""

    return saved.isNotEmpty() &&
            answer.trim().equals(
                saved.trim(),
                ignoreCase = true
            )
}

fun changePassword(
    password: String
) {
    preferences()
        .edit()
        .putString(KEY_PASSWORD, password)
        .apply()
}

fun setSecurityAnswer(
    answer: String
) {
    preferences()
        .edit()
        .putString(KEY_SECURITY, answer)
        .apply()
}

private fun loadHiddenApps() {
    val saved =
        preferences()
            .getStringSet(
                "hidden_apps",
                emptySet()
            )
            ?: emptySet()

    hiddenPackages.clear()
    hiddenPackages.addAll(saved)
}

private fun saveHiddenApps() {
    preferences()
        .edit()
        .putStringSet(
            "hidden_apps",
            hiddenPackages.toSet()
        )
        .apply()
}

fun toggleHidden(
    packageName: String
) {
    if (hiddenPackages.contains(packageName)) {
        hiddenPackages.remove(packageName)
    } else {
        hiddenPackages.add(packageName)
    }

    saveHiddenApps()
}

fun isHidden(
    packageName: String
): Boolean {
    return hiddenPackages.contains(packageName)
}

fun launchPackage(
    packageName: String
) {
    try {
        val intent =
            packageManager.getLaunchIntentForPackage(
                packageName
            )

        if (intent != null) {
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
            startActivity(intent)
        }
    } catch (_: Exception) {
    }
}

fun installedApps(): List<InstalledApp> {
    val pm = packageManager

    return pm.getInstalledApplications(
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
                label = pm.getApplicationLabel(app)
                    .toString(),
                icon = pm.getApplicationIcon(app)
            )
        }
        .sortedBy {
            it.label.lowercase(Locale.getDefault())
        }
}

}

@OptIn(ExperimentalAnimationApi::class)@Composablefun DarkCalcApp(activity: MainActivity) {var screen by remember {mutableStateOf("home")}

var page by remember {
    mutableStateOf(0)
}

var direction by remember {
    mutableStateOf(1)
}

val allApps =
    remember {
        activity.installedApps()
    }

val visibleApps =
    allApps.filter {
        !activity.isHidden(it.packageName)
    }

val hiddenApps =
    allApps.filter {
        activity.isHidden(it.packageName)
    }

val appsPerPage = 12

val pages =
    visibleApps.chunked(appsPerPage)

fun movePage(
    newPage: Int
) {
    if (pages.isEmpty()) return

    val target =
        newPage.coerceIn(
            0,
            pages.lastIndex
        )

    if (target == page) return

    direction =
        if (target > page) 1 else -1

    page = target
}

AnimatedContent(
    targetState = screen,
    transitionSpec = {
        if (targetState == "home") {
            slideInHorizontally { -it / 3 } +
                    fadeIn() togetherWith
                    slideOutHorizontally { it / 3 } +
                    fadeOut()
        } else {
            slideInHorizontally { it / 3 } +
                    fadeIn() togetherWith
                    slideOutHorizontally { -it / 3 } +
                    fadeOut()
        }.using(
            SizeTransform(
                clip = false
            )
        )
    },
    label = "screen_animation"
) { currentScreen ->

    when (currentScreen) {

        "home" -> {

            HomeScreen(
                apps = pages.getOrNull(page)
                    ?: emptyList(),
                page = page,
                pageCount = pages.size,
                onAppClick = {
                    activity.launchPackage(
                        it
                    )
                },
                onCalculator = {
                    screen = "calculator"
                },
                onSwipeLeft = {
                    movePage(page + 1)
                },
                onSwipeRight = {
                    movePage(page - 1)
                },
                onLogin = {
                    screen = "login"
                },
                onHidden = {
                    screen = "hidden"
                }
            )
        }

        "calculator" -> {

            CalculatorScreen(
                onBack = {
                    screen = "home"
                },
                onSecretCode = {
                    screen = "login"
                }
            )
        }

        "login" -> {

            LoginScreen(
                activity = activity,
                onBack = {
                    screen = "home"
                },
                onSuccess = {
                    screen = "hidden"
                },
                onReset = {
                    screen = "reset"
                }
            )
        }

        "reset" -> {

            ResetPasswordScreen(
                activity = activity,
                onBack = {
                    screen = "login"
                },
                onDone = {
                    screen = "login"
                }
            )
        }

        "hidden" -> {

            HiddenAppsScreen(
                apps = hiddenApps,
                onLaunch = {
                    activity.launchPackage(it)
                },
                onBack = {
                    screen = "home"
                }
            )
        }
    }
}

}

@Composablefun HomeScreen(apps: List<InstalledApp>,page: Int,pageCount: Int,onAppClick: (String) -> Unit,onCalculator: () -> Unit,onSwipeLeft: () -> Unit,onSwipeRight: () -> Unit,onLogin: () -> Unit,onHidden: () -> Unit) {var dragAmount by remember {mutableStateOf(0f)}

Box(
    modifier = Modifier
        .fillMaxSize()
        .background(
            MaterialTheme.colorScheme.background
        )
        .pointerInput(page) {

            detectHorizontalDragGestures(

                onHorizontalDrag = { _, amount ->
                    dragAmount += amount
                },

                onDragEnd = {

                    if (dragAmount < -80f) {
                        onSwipeLeft()
                    } else if (dragAmount > 80f) {
                        onSwipeRight()
                    }

                    dragAmount = 0f
                }
            )
        }
        .padding(
            horizontal = 16.dp,
            vertical = 18.dp
        )
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        LiveClock()

        LiveDate()

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp),
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
                        onAppClick(
                            app.packageName
                        )
                    }
                )
            }

            item {

                AppIcon(
                    app = InstalledApp(
                        packageName =
                            CALCULATOR_PACKAGE,
                        label = "ماشین حساب",
                        icon =
                            android.graphics.drawable.ColorDrawable(
                                android.graphics.Color.DKGRAY
                            )
                    ),
                    onClick = onCalculator
                )
            }
        }

        PageIndicator(
            count = pageCount,
            current = page
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Dock(
            onLogin = onLogin,
            onHidden = onHidden
        )
    }
}

}

@Composablefun AppIcon(app: InstalledApp,onClick: () -> Unit) {Column(modifier = Modifier.fillMaxWidth().clickable {onClick()},horizontalAlignment =Alignment.CenterHorizontally) {

    Surface(
        modifier = Modifier.size(64.dp),
        shape = MaterialTheme.shapes.medium
    ) {

        androidx.compose.foundation.Image(
            bitmap = app.icon
                .toBitmap()
                .asImageBitmap(),
            contentDescription =
                app.label,
            contentScale =
                ContentScale.Fit,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(4.dp)
        )
    }

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

private fun Drawable.toBitmap(): android.graphics.Bitmap {val width =if (intrinsicWidth > 0)intrinsicWidthelse 64

val height =
    if (intrinsicHeight > 0)
        intrinsicHeight
    else 64

val bitmap =
    android.graphics.Bitmap.createBitmap(
        width,
        height,
        android.graphics.Bitmap.Config.ARGB_8888
    )

val canvas =
    android.graphics.Canvas(bitmap)

setBounds(
    0,
    0,
    canvas.width,
    canvas.height
)

draw(canvas)

return bitmap

}

@Composablefun PageIndicator(count: Int,current: Int) {if (count <= 1) return

Row(
    modifier = Modifier.fillMaxWidth(),
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
            fontSize = 11.sp,
            modifier =
                Modifier.padding(
                    horizontal = 3.dp
                )
        )
    }
}

}

@Composablefun Dock(onLogin: () -> Unit,onHidden: () -> Unit) {Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp,bottom = 4.dp),horizontalArrangement =Arrangement.SpaceEvenly) {

    DockButton(
        text = "⌕",
        label = "ورود",
        onClick = onLogin
    )

    DockButton(
        text = "▣",
        label = "مخفی",
        onClick = onHidden
    )
}

}

@Composablefun DockButton(text: String,label: String,onClick: () -> Unit) {Column(modifier = Modifier.clickable {onClick()}.padding(8.dp),horizontalAlignment =Alignment.CenterHorizontally) {

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

@Composablefun LiveClock() {

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
    fontWeight = FontWeight.Light
)

}

@Composablefun LiveDate() {

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
    fontSize = 14.sp
)

}

@Composablefun CalculatorScreen(onBack: () -> Unit,onSecretCode: () -> Unit) {

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

            if (display == "2580") {

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
    modifier = Modifier
        .fillMaxSize()
        .imePadding()
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
        modifier =
            Modifier.height(30.dp)
    )

    Text(
        text =
            if (display.isEmpty())
                "0"
            else
                display,
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

fun calculateSimple(expression: String): String {

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

@Composablefun LoginScreen(activity: MainActivity,onBack: () -> Unit,onSuccess: () -> Unit,onReset: () -> Unit) {

var username by remember {
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
    horizontalAlignment = Alignment.CenterHorizontally
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
        shape = RoundedCornerShape(12.dp)
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
            color = MaterialTheme
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