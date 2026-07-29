package com.example.data.repository

import android.content.Context
import com.example.data.db.dao.BookmarkDao
import com.example.data.db.entities.BookmarkEntity
import com.example.domain.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class FileRepository(
    private val context: Context,
    private val bookmarkDao: BookmarkDao
) {
    val bookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    fun getRootDirectory(): File = context.filesDir

    suspend fun listFiles(dirPath: String): List<FileItem> = withContext(Dispatchers.IO) {
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) return@withContext emptyList()

        val files = dir.listFiles() ?: return@withContext emptyList()
        return@withContext files.map { file ->
            FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                sizeBytes = if (file.isDirectory) 0 else file.length(),
                lastModified = file.lastModified()
            )
        }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    suspend fun readFileText(filePath: String): String = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists() || file.isDirectory) return@withContext ""
        return@withContext file.readText(Charsets.UTF_8)
    }

    suspend fun writeFileText(filePath: String, content: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(filePath)
            file.parentFile?.mkdirs()
            file.writeText(content, Charsets.UTF_8)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createNewFile(parentDirPath: String, fileName: String): FileItem? = withContext(Dispatchers.IO) {
        try {
            val file = File(parentDirPath, fileName)
            file.parentFile?.mkdirs()
            if (file.createNewFile()) {
                FileItem(file.name, file.absolutePath, false, 0, file.lastModified())
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createNewFolder(parentDirPath: String, folderName: String): FileItem? = withContext(Dispatchers.IO) {
        try {
            val dir = File(parentDirPath, folderName)
            if (dir.mkdirs() || dir.exists()) {
                FileItem(dir.name, dir.absolutePath, true, 0, dir.lastModified())
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun renameFile(oldPath: String, newName: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(oldPath)
            val newFile = File(file.parentFile, newName)
            file.renameTo(newFile)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun zipDirectory(dirPath: String, zipFilePath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val sourceDir = File(dirPath)
            val zipFile = File(zipFilePath)
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                sourceDir.walk().forEach { file ->
                    if (!file.isDirectory) {
                        val relPath = sourceDir.toPath().relativize(file.toPath()).toString()
                        zos.putNextEntry(ZipEntry(relPath))
                        FileInputStream(file).use { fis -> fis.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun unzipFile(zipFilePath: String, destDirPath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val destDir = File(destDirPath)
            destDir.mkdirs()
            ZipInputStream(FileInputStream(zipFilePath)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val newFile = File(destDir, entry.name)
                    if (entry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        FileOutputStream(newFile).use { fos -> zis.copyTo(fos) }
                    }
                    entry = zis.nextEntry
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleBookmark(filePath: String, alias: String, isDirectory: Boolean) {
        val file = File(filePath)
        if (!file.exists()) return
        bookmarkDao.insertBookmark(BookmarkEntity(filePath, alias.ifBlank { file.name }, isDirectory))
    }

    suspend fun removeBookmark(filePath: String) {
        bookmarkDao.deleteBookmark(filePath)
    }
}
