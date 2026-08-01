/* =========================================================
   PAGE INDICATOR
   ========================================================= */

@Composable
fun PageIndicator(
    count: Int,
    current: Int
) {
    if (count <= 1) {
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {

        repeat(count) { index ->

            Text(Text(
                text = if (index == current) {
                    "●"
                } else {
                    "○"
                },
                fontSize = 11.sp,
                modifier = Modifier.padding(
                    horizontal = 3.dp
                )
            )
        }
    }
}


/* =========================================================
   APP ICON
   ========================================================= */

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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = app.icon.asImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier.size(58.dp)
        )

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


/* =========================================================
   APP DRAWER
   ========================================================= */

@Composable
fun AppDrawer(
    apps: List<InstalledApp>,
    calculator: InstalledApp,
    onLaunch: (String) -> Unit,
    onCalculator: () -> Unit,
    onBack: () -> Unit
) {
    val drawerApps = listOf(calculator) + apps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "برنامه‌ها",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            items(
                items = drawerApps,
                key = { app ->
                    app.packageName
                }
            ) { app ->

                AppIcon(
                    app = app,
                    onClick = {
                        if (app.isCalculator) {
                            onCalculator()
                        } else {
                            onLaunch(app.packageName)
                        }
                    }
                )
            }
        }
    }
}


/* =========================================================
   DOCK
   ========================================================= */

@Composable
fun Dock(
    onDrawer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface
            )
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {

        DockButton(
            text = "▦",
            label = "برنامه‌ها",
            onClick = onDrawer
        )
    }
}


/* =========================================================
   DOCK BUTTON
   ========================================================= */

@Composable
fun DockButton(
    text: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

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


/* =========================================================
   LIVE CLOCK
   ========================================================= */

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
        fontSize = 44.sp,
        fontWeight = FontWeight.Light
    )
}


/* =========================================================
   LIVE DATE
   ========================================================= */

@Composable
fun LiveDate() {

    var date by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        while (true) {

            date = SimpleDateFormat(
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


/* =========================================================
   CALCULATOR
   ========================================================= */

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
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
            modifier = Modifier.height(30.dp)
        )

        Text(
            text = if (display.isEmpty()) {
                "0"
            } else {
                display
            },
            fontSize = 38.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        rows.forEach { row ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                row.forEach { value ->

                    Button(
                        onClick = {
                            press(value)
                        },
                        modifier = Modifier
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
                modifier = Modifier.height(8.dp)
            )
        }
    }
}


/* =========================================================
   CALCULATOR ENGINE
   ========================================================= */

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
        } else if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            result.toString()
        }

    } catch (_: Exception) {

        "خطا"
    }
}


/* =========================================================
   LOGIN
   ========================================================= */

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
            .padding(horizontal = 28.dp)
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
                shape =
                    RoundedCornerShape(12.dp)
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp)
        ) {

            Text("بازگشت")
        }
    }
}


/* =========================================================
   RESET PASSWORD
   ========================================================= */

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
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier = Modifier.height(55.dp)
        )

        Text(
            text = "بازنشانی رمز",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = SECURITY_QUESTION,
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
            modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("تکرار رمز")
            },
            singleLine = true,
            visualTransformation =
                PasswordVisualTransformation()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
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
                modifier = Modifier.height(12.dp)
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


/* =========================================================
   HIDDEN APPS
   ========================================================= */

@Composable
fun HiddenAppsScreen(
    apps: List<InstalledApp>,
    onLaunch: (String) -> Unit,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
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
                text = "برنامه‌های مخفی",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
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
                    GridCells.Fixed(3),

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp),

                verticalArrangement =
                    Arrangement.spacedBy(20.dp)
            ) {

                items(
                    items = apps,
                    key = { app ->
                        app.packageName
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