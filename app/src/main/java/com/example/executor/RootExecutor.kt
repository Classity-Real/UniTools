package com.example.executor

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val message: String,
    val isError: Boolean = false,
    val isCommand: Boolean = false,
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
)

data class ExecutionResult(
    val exitCode: Int,
    val isSuccess: Boolean,
    val durationMs: Long
)

class RootExecutor {
    private val _logs = MutableSharedFlow<LogEntry>(replay = 200, extraBufferCapacity = 500)
    val logs: SharedFlow<LogEntry> = _logs.asSharedFlow()

    @Volatile
    private var currentProcess: Process? = null

    suspend fun checkRootAccess(): Boolean = withContext(Dispatchers.IO) {
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )
        val fileFound = suPaths.any { File(it).exists() }

        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id -u"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            process.waitFor()
            process.exitValue() == 0 && (line?.trim() == "0" || fileFound)
        } catch (e: Exception) {
            Log.w("RootExecutor", "Root check failed: ${e.message}")
            fileFound
        }
    }

    suspend fun executeCommand(
        command: String,
        workingDir: File? = null,
        onLogLine: ((LogEntry) -> Unit)? = null
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val cmdEntry = LogEntry(message = "$ $command", isCommand = true)
        _logs.emit(cmdEntry)
        onLogLine?.invoke(cmdEntry)

        try {
            val processBuilder = ProcessBuilder("su")
            processBuilder.redirectErrorStream(false)

            val process = processBuilder.start()
            currentProcess = process

            val writer = process.outputStream.bufferedWriter()

            val appDir = workingDir?.absolutePath ?: "/data/local/tmp/unitools"
            val tmpDir = "/data/local/tmp/unitools"

            // Clean command: strip leading ./ if present so it matches our shell functions cleanly
            val cleanCmd = if (command.startsWith("./")) command.removePrefix("./") else command

            val shellScript = buildString {
                appendLine("# 1. Staging directory setup")
                appendLine("mkdir -p \"$tmpDir\" 2>/dev/null")
                appendLine("if [ -d \"$appDir\" ] && [ \"$appDir\" != \"$tmpDir\" ]; then")
                appendLine("    cp -rf \"$appDir\"/* \"$tmpDir\"/ 2>/dev/null")
                appendLine("fi")
                appendLine("cd \"$tmpDir\" 2>/dev/null || cd \"$appDir\" 2>/dev/null")
                appendLine("chmod -R 777 \"$tmpDir\" 2>/dev/null")
                appendLine("chmod 755 ./spd_dump ./gen_spl-unlock ./chsize ./pacextractor 2>/dev/null")
                appendLine("chmod 755 \"$tmpDir/glibc/lib/ld-linux-aarch64.so.1\" 2>/dev/null")
                appendLine("chmod 755 \"$tmpDir/glibc/bin/bash\" 2>/dev/null")

                // Locate glibc loader and libs
                appendLine("GLIBC_LOADER=\"\"")
                appendLine("if [ -f \"$tmpDir/glibc/lib/ld-linux-aarch64.so.1\" ]; then")
                appendLine("    GLIBC_LOADER=\"$tmpDir/glibc/lib/ld-linux-aarch64.so.1\"")
                appendLine("    GLIBC_LIB=\"$tmpDir/glibc/lib\"")
                appendLine("elif [ -f \"$appDir/glibc/lib/ld-linux-aarch64.so.1\" ]; then")
                appendLine("    GLIBC_LOADER=\"$appDir/glibc/lib/ld-linux-aarch64.so.1\"")
                appendLine("    GLIBC_LIB=\"$appDir/glibc/lib\"")
                appendLine("elif [ -f \"/lib/ld-linux-aarch64.so.1\" ]; then")
                appendLine("    GLIBC_LOADER=\"/lib/ld-linux-aarch64.so.1\"")
                appendLine("    GLIBC_LIB=\"/lib\"")
                appendLine("fi")

                // Attempt to link /lib/ld-linux-aarch64.so.1 if system rootfs is writable
                appendLine("if [ -n \"\$GLIBC_LOADER\" ] && [ ! -f /lib/ld-linux-aarch64.so.1 ]; then")
                appendLine("    mount -o remount,rw / 2>/dev/null")
                appendLine("    mkdir -p /lib 2>/dev/null")
                appendLine("    ln -sf \"\$GLIBC_LOADER\" /lib/ld-linux-aarch64.so.1 2>/dev/null")
                appendLine("    ln -sf \"\$GLIBC_LIB/\"* /lib/ 2>/dev/null")
                appendLine("fi")

                appendLine("export PATH=\"$tmpDir:\$PATH\"")
                appendLine("if [ -n \"\$GLIBC_LIB\" ]; then")
                appendLine("    export LD_LIBRARY_PATH=\"\$GLIBC_LIB:\$LD_LIBRARY_PATH\"")
                appendLine("fi")

                // Shell functions for spd_dump and tools: NEVER use sed replacements to prevent path doubling!
                appendLine("spd_dump() {")
                appendLine("    if [ -f \"/lib/ld-linux-aarch64.so.1\" ]; then")
                appendLine("        \"$tmpDir/spd_dump\" \"\$@\"")
                appendLine("    elif [ -n \"\$GLIBC_LOADER\" ] && [ -f \"\$GLIBC_LOADER\" ]; then")
                appendLine("        \"\$GLIBC_LOADER\" --library-path \"\$GLIBC_LIB\" \"$tmpDir/spd_dump\" \"\$@\"")
                appendLine("    else")
                appendLine("        \"$tmpDir/spd_dump\" \"\$@\"")
                appendLine("    fi")
                appendLine("}")

                appendLine("gen_spl-unlock() {")
                appendLine("    if [ -f \"/lib/ld-linux-aarch64.so.1\" ]; then")
                appendLine("        \"$tmpDir/gen_spl-unlock\" \"\$@\"")
                appendLine("    elif [ -n \"\$GLIBC_LOADER\" ] && [ -f \"\$GLIBC_LOADER\" ]; then")
                appendLine("        \"\$GLIBC_LOADER\" --library-path \"\$GLIBC_LIB\" \"$tmpDir/gen_spl-unlock\" \"\$@\"")
                appendLine("    else")
                appendLine("        \"$tmpDir/gen_spl-unlock\" \"\$@\"")
                appendLine("    fi")
                appendLine("}")

                appendLine("chsize() {")
                appendLine("    if [ -f \"/lib/ld-linux-aarch64.so.1\" ]; then")
                appendLine("        \"$tmpDir/chsize\" \"\$@\"")
                appendLine("    elif [ -n \"\$GLIBC_LOADER\" ] && [ -f \"\$GLIBC_LOADER\" ]; then")
                appendLine("        \"\$GLIBC_LOADER\" --library-path \"\$GLIBC_LIB\" \"$tmpDir/chsize\" \"\$@\"")
                appendLine("    else")
                appendLine("        \"$tmpDir/chsize\" \"\$@\"")
                appendLine("    fi")
                appendLine("}")

                appendLine("pacextractor() {")
                appendLine("    if [ -f \"/lib/ld-linux-aarch64.so.1\" ]; then")
                appendLine("        \"$tmpDir/pacextractor\" \"\$@\"")
                appendLine("    elif [ -n \"\$GLIBC_LOADER\" ] && [ -f \"\$GLIBC_LOADER\" ]; then")
                appendLine("        \"\$GLIBC_LOADER\" --library-path \"\$GLIBC_LIB\" \"$tmpDir/pacextractor\" \"\$@\"")
                appendLine("    else")
                appendLine("        \"$tmpDir/pacextractor\" \"\$@\"")
                appendLine("    fi")
                appendLine("}")

                // Execute the clean command (calls the spd_dump function directly with all arguments)
                appendLine("$cleanCmd")
                appendLine("exit $?")
            }

            writer.write(shellScript)
            writer.flush()

            // Stream stdout
            val stdoutThread = Thread {
                try {
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val entry = LogEntry(message = line ?: "")
                        _logs.tryEmit(entry)
                        onLogLine?.invoke(entry)
                    }
                } catch (ignored: Exception) {}
            }
            stdoutThread.start()

            // Stream stderr
            val stderrThread = Thread {
                try {
                    val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                    var errLine: String?
                    while (errorReader.readLine().also { errLine = it } != null) {
                        val entry = LogEntry(message = errLine ?: "", isError = true)
                        _logs.tryEmit(entry)
                        onLogLine?.invoke(entry)
                    }
                } catch (ignored: Exception) {}
            }
            stderrThread.start()

            val exitCode = process.waitFor()
            stdoutThread.join(1000)
            stderrThread.join(1000)
            currentProcess = null

            val duration = System.currentTimeMillis() - startTime
            val statusMessage = if (exitCode == 0) {
                "[FINISHED] Command executed successfully (code 0 in ${duration}ms)"
            } else {
                "[FAILED] Process exited with status code $exitCode (${duration}ms)"
            }
            val completionEntry = LogEntry(message = statusMessage, isError = exitCode != 0)
            _logs.emit(completionEntry)
            onLogLine?.invoke(completionEntry)

            ExecutionResult(
                exitCode = exitCode,
                isSuccess = exitCode == 0,
                durationMs = duration
            )
        } catch (e: Exception) {
            currentProcess = null
            val duration = System.currentTimeMillis() - startTime
            val errEntry = LogEntry(message = "[ERROR] Execution exception: ${e.localizedMessage}", isError = true)
            _logs.emit(errEntry)
            onLogLine?.invoke(errEntry)
            ExecutionResult(exitCode = -1, isSuccess = false, durationMs = duration)
        }
    }

    fun cancel() {
        try {
            currentProcess?.destroy()
            currentProcess = null
            _logs.tryEmit(LogEntry(message = "[ABORTED] Operation terminated by user.", isError = true))
        } catch (e: Exception) {
            Log.e("RootExecutor", "Error destroying process", e)
        }
    }
}
