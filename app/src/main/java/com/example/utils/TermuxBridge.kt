package com.example.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

data class TermuxStatus(
    val isTermuxInstalled: Boolean,
    val isTermuxApiInstalled: Boolean,
    val hasRunCommandPermission: Boolean,
    val termuxVersionName: String? = null
)

object TermuxBridge {
    private const val TAG = "TermuxBridge"
    const val TERMUX_PACKAGE = "com.termux"
    const val TERMUX_API_PACKAGE = "com.termux.api"
    const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
    const val RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"

    // Termux Intent Extras Keys
    const val EXTRA_RUN_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
    const val EXTRA_RUN_COMMAND_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
    const val EXTRA_RUN_COMMAND_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
    const val EXTRA_RUN_COMMAND_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
    const val EXTRA_RUN_COMMAND_SESSION_ACTION = "com.termux.RUN_COMMAND_SESSION_ACTION"

    /**
     * Checks if Termux and Termux:API packages are present on the host device.
     */
    fun checkTermuxStatus(context: Context): TermuxStatus {
        val pm = context.packageManager
        var isInstalled = false
        var isApiInstalled = false
        var versionName: String? = null

        try {
            val info = pm.getPackageInfo(TERMUX_PACKAGE, 0)
            isInstalled = true
            versionName = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            isInstalled = false
        }

        try {
            pm.getPackageInfo(TERMUX_API_PACKAGE, 0)
            isApiInstalled = true
        } catch (e: PackageManager.NameNotFoundException) {
            isApiInstalled = false
        }

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission("com.termux.permission.RUN_COMMAND") == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return TermuxStatus(
            isTermuxInstalled = isInstalled,
            isTermuxApiInstalled = isApiInstalled,
            hasRunCommandPermission = hasPermission,
            termuxVersionName = versionName
        )
    }

    /**
     * Executes a command inside Termux via the com.termux.RUN_COMMAND Intent service.
     */
    fun executeCommand(
        context: Context,
        commandPath: String = "/data/data/com.termux/files/usr/bin/bash",
        arguments: Array<String> = emptyArray(),
        workDir: String? = "/data/data/com.termux/files/home",
        background: Boolean = false,
        sessionAction: String = "0"
    ): Boolean {
        return try {
            val intent = Intent(ACTION_RUN_COMMAND).apply {
                setClassName(TERMUX_PACKAGE, RUN_COMMAND_SERVICE)
                putExtra(EXTRA_RUN_COMMAND_PATH, commandPath)
                if (arguments.isNotEmpty()) {
                    putExtra(EXTRA_RUN_COMMAND_ARGUMENTS, arguments)
                }
                if (!workDir.isNull_or_blank()) {
                    putExtra(EXTRA_RUN_COMMAND_WORKDIR, workDir)
                }
                putExtra(EXTRA_RUN_COMMAND_BACKGROUND, background)
                putExtra(EXTRA_RUN_COMMAND_SESSION_ACTION, sessionAction)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "Sent RUN_COMMAND intent to Termux: $commandPath ${arguments.joinToString(" ")}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send RUN_COMMAND intent to Termux", e)
            false
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

    /**
     * Executes a Termux shell string by passing it to bash -c "..."
     */
    fun executeShellScript(
        context: Context,
        script: String,
        workDir: String? = "/data/data/com.termux/files/home",
        background: Boolean = true
    ): Boolean {
        return executeCommand(
            context = context,
            commandPath = "/data/data/com.termux/files/usr/bin/bash",
            arguments = arrayOf("-c", script),
            workDir = workDir,
            background = background
        )
    }

    /**
     * Invokes Termux:API command line tool (e.g., termux-toast, termux-vibrate, termux-battery-status)
     */
    fun callTermuxApiTool(
        context: Context,
        apiToolName: String,
        arguments: Array<String> = emptyArray()
    ): Boolean {
        val fullToolPath = "/data/data/com.termux/files/usr/bin/$apiToolName"
        return executeCommand(
            context = context,
            commandPath = fullToolPath,
            arguments = arguments,
            background = true
        )
    }

    /**
     * Launches the main Termux application.
     */
    fun openTermuxApp(context: Context): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
