package com.example.manager

import android.content.Context
import android.util.Log
import com.example.model.DeviceBrand
import com.example.model.SocType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class BinaryFileManager(private val context: Context) {
    val binariesDir: File = File(context.filesDir, "binaries")
    val backupDir: File = File(context.filesDir, "backup")
    val inputDir: File = File(context.filesDir, "input")
    val backupSplDir: File = File(context.filesDir, "backup_spl")
    val localTmpDir: File = File("/data/local/tmp/unitools")

    private val versionFile: File = File(binariesDir, ".binaries_installed_v3")

    init {
        binariesDir.mkdirs()
        backupDir.mkdirs()
        inputDir.mkdirs()
        backupSplDir.mkdirs()
    }

    suspend fun ensureBinariesReady(onProgress: (String) -> Unit): Boolean = withContext(Dispatchers.IO) {
        try {
            val glibcLoader = File(binariesDir, "glibc/lib/ld-linux-aarch64.so.1")
            if (versionFile.exists() && File(binariesDir, "spd_dump").exists() && glibcLoader.exists()) {
                onProgress("Binaries verified in app storage.")
                syncToLocalTmp()
                return@withContext true
            }

            onProgress("Extracting Unisoc flashing toolchain & glibc runtime...")
            copyAssetFolder("spreadtrum_files", binariesDir)

            if (!File(binariesDir, "spd_dump").exists()) {
                copyAssetFolder("", binariesDir)
            }

            setExecutablePermissions()
            syncToLocalTmp()
            versionFile.writeText("installed_v3")
            onProgress("Toolchain and glibc runtime ready.")
            true
        } catch (e: Exception) {
            Log.e("BinaryFileManager", "Failed to extract binaries", e)
            onProgress("Error initializing toolchain: ${e.message}")
            false
        }
    }

    fun syncToLocalTmp() {
        try {
            Runtime.getRuntime().exec(
                arrayOf(
                    "su", "-c",
                    "mkdir -p /data/local/tmp/unitools && cp -rf ${binariesDir.absolutePath}/* /data/local/tmp/unitools/ 2>/dev/null && chmod -R 777 /data/local/tmp/unitools 2>/dev/null"
                )
            ).waitFor()
        } catch (e: Exception) {
            Log.w("BinaryFileManager", "syncToLocalTmp warning: ${e.message}")
        }
    }

    fun prepareFdlFiles(soc: SocType, brand: DeviceBrand) {
        try {
            // Remove old FDLs in root of binariesDir
            binariesDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("fdl") || file.name.startsWith("custom_exec")) {
                    file.delete()
                }
            }

            // Look for device specific FDL files
            val socFolder = File(binariesDir, soc.id)
            val brandFolder = File(socFolder, brand.id)
            val sourceDir = if (brandFolder.exists()) brandFolder else socFolder

            if (sourceDir.exists()) {
                sourceDir.listFiles()?.forEach { src ->
                    if (src.isFile) {
                        src.copyTo(File(binariesDir, src.name), overwrite = true)
                    }
                }
            }

            // If fdl1-dl.bin still not in binariesDir, check if it's in default root binariesDir
            val fallbackFdl1 = File(binariesDir, "fdl1-dl.bin")
            if (!fallbackFdl1.exists()) {
                socFolder.listFiles()?.find { it.name.contains("fdl1") }?.copyTo(fallbackFdl1, overwrite = true)
            }

            syncToLocalTmp()
        } catch (e: Exception) {
            Log.e("BinaryFileManager", "Error preparing FDL files", e)
        }
    }

    private fun copyAssetFolder(assetPath: String, destDir: File) {
        val assetManager = context.assets
        val files = assetManager.list(assetPath) ?: return

        for (file in files) {
            val subAssetPath = if (assetPath.isEmpty()) file else "$assetPath/$file"
            val subFiles = assetManager.list(subAssetPath)

            if (!subFiles.isNullOrEmpty()) {
                val subDestDir = File(destDir, file)
                subDestDir.mkdirs()
                copyAssetFolder(subAssetPath, subDestDir)
            } else {
                val destFile = File(destDir, file)
                try {
                    assetManager.open(subAssetPath).use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("BinaryFileManager", "Skip non-file or directory $subAssetPath: ${e.message}")
                }
            }
        }
    }

    private fun setExecutablePermissions() {
        val executables = listOf(
            "spd_dump",
            "gen_spl-unlock",
            "gen_spl-unlock-legacy",
            "chsize",
            "pacextractor",
            "glibc/lib/ld-linux-aarch64.so.1",
            "glibc/bin/bash"
        )
        for (binName in executables) {
            val binFile = File(binariesDir, binName)
            if (binFile.exists()) {
                binFile.setExecutable(true, false)
                binFile.setReadable(true, false)
                binFile.setWritable(true, true)
            }
        }
        try {
            Runtime.getRuntime().exec(arrayOf("chmod", "-R", "777", binariesDir.absolutePath))
        } catch (ignored: Exception) {}
    }
}
