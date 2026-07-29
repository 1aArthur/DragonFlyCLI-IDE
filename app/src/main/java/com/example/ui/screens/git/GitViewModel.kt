package com.example.ui.screens.git

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.utils.GitHelper
import kotlinx.coroutines.launch

class GitViewModel(private val gitHelper: GitHelper) : ViewModel() {

    fun status() {
        viewModelScope.launch { gitHelper.status() }
    }

    fun clone(url: String) {
        viewModelScope.launch { gitHelper.clone(url) }
    }

    fun addAll() {
        viewModelScope.launch { gitHelper.addAll() }
    }

    fun commit(msg: String) {
        viewModelScope.launch { gitHelper.commit(msg) }
    }

    fun push() {
        viewModelScope.launch { gitHelper.push() }
    }

    fun pull() {
        viewModelScope.launch { gitHelper.pull() }
    }

    fun log() {
        viewModelScope.launch { gitHelper.log() }
    }

    fun diff() {
        viewModelScope.launch { gitHelper.diff() }
    }

    fun listBranches() {
        viewModelScope.launch { gitHelper.listBranches() }
    }
}
