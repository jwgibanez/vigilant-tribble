package io.github.jwgibanez.stb.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UITest {

    private val PACKAGE = "io.github.jwgibanez.stb"
    private val LAUNCH_TIMEOUT = 5000L

    private lateinit var device: UiDevice

    @Before
    fun startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from the home screen
        device.pressHome()

        // Wait for launcher
        val launcherPackage: String = getLauncherPackageName()
        ViewMatchers.assertThat(launcherPackage, CoreMatchers.notNullValue())
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT)

        // Launch the app
        val context: Context = ApplicationProvider.getApplicationContext()
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
        context.startActivity(intent)

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), LAUNCH_TIMEOUT)
    }

    @Test
    fun checkPreconditions() {
        ViewMatchers.assertThat(device, CoreMatchers.notNullValue())
    }

    @Test
    fun openBarcodeScanner() {
        device.apply {
            findObject(By.res(PACKAGE, "start_button")).click()
            Thread.sleep(500)

            val preview = findObject(By.res(PACKAGE, "view_finder"))
            assertThat("Preview is showing", preview != null)

            pressBack()

            val startButton = findObject(By.res(PACKAGE, "start_button"))
            assertThat("Back to main page", startButton != null)
        }
    }

    private fun getLauncherPackageName(): String {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)

        val pm = ApplicationProvider.getApplicationContext<Context>().packageManager
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo!!.activityInfo.packageName
    }
}