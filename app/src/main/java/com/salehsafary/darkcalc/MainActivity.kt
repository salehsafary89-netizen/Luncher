private fun getInstalledApps(): List<InstalledApp> {

    val pm = packageManager

    return pm.getInstalledApplications(
        PackageManager.GET_META_DATA
    )
        .asSequence()

        // فقط برنامه‌هایی که واقعاً قابل اجرا هستند
        .filter { appInfo ->
            pm.getLaunchIntentForPackage(
                appInfo.packageName
            ) != null
        }

        // خود DarkCalc نمایش داده نشود
        .filter { appInfo ->
            appInfo.packageName != packageName
        }

        // تلگرام عمداً در Home و App Drawer نمایش داده نشود
        .filter { appInfo ->
            appInfo.packageName != "org.telegram.messenger"
        }

        // ساخت اطلاعات برنامه
        .map { appInfo ->

            InstalledApp(
                label = pm
                    .getApplicationLabel(appInfo)
                    .toString(),

                packageName =
                    appInfo.packageName,

                icon =
                    drawableToBitmap(
                        appInfo.loadIcon(pm)
                    )
            )
        }

        // جلوگیری از برنامه‌های تکراری
        .distinctBy {
            it.packageName
        }

        // مرتب‌سازی بر اساس نام
        .sortedBy {
            it.label.lowercase()
        }

        .toList()
}