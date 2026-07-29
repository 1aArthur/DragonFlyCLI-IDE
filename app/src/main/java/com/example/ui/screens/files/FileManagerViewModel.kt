package com.example.ui.screens.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.entities.BookmarkEntity
import com.example.data.repository.FileRepository
import com.example.domain.model.FileItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class FileManagerViewModel(val fileRepository: FileRepository) : ViewModel() {
    private val _currentPath = MutableStateFlow(fileRepository.getRootDirectory().absolutePath)
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> = fileRepository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchFilter = MutableStateFlow("")
    val searchFilter: StateFlow<String> = _searchFilter.asStateFlow()

    init {
        loadDirectory(_currentPath.value)
    }

    fun setSearchFilter(query: String) {
        _searchFilter.value = query
    }

    fun loadDirectory(path: String) {
        viewModelScope.launch {
            _currentPath.value = path
            _files.value = fileRepository.listFiles(path)
        }
    }

    fun renameFile(item: FileItem, newName: String) {
        viewModelScope.launch {
            fileRepository.renameFile(item.path, newName)
            loadDirectory(_currentPath.value)
        }
    }

    fun navigateUp() {
        val current = File(_currentPath.value)
        val parent = current.parentFile
        if (parent != null && parent.exists()) {
            loadDirectory(parent.absolutePath)
        }
    }

    fun createNewFile(fileName: String) {
        viewModelScope.launch {
            fileRepository.createNewFile(_currentPath.value, fileName)
            loadDirectory(_currentPath.value)
        }
    }

    fun createNewFolder(folderName: String) {
        viewModelScope.launch {
            fileRepository.createNewFolder(_currentPath.value, folderName)
            loadDirectory(_currentPath.value)
        }
    }

    fun deleteFile(item: FileItem) {
        viewModelScope.launch {
            fileRepository.deleteFile(item.path)
            loadDirectory(_currentPath.value)
        }
    }

    fun toggleBookmark(item: FileItem) {
        viewModelScope.launch {
            fileRepository.toggleBookmark(item.path, item.name, item.isDirectory)
        }
    }

    fun zipDirectory(item: FileItem) {
        viewModelScope.launch {
            val zipPath = "${item.path}.zip"
            fileRepository.zipDirectory(item.path, zipPath)
            loadDirectory(_currentPath.value)
        }
    }

    fun unzipFile(item: FileItem) {
        viewModelScope.launch {
            val destPath = item.path.removeSuffix(".zip")
            fileRepository.unzipFile(item.path, destPath)
            loadDirectory(_currentPath.value)
        }
    }
}
