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

    fun push(remote: String = "origin", branch: String = "main") {
        viewModelScope.launch { gitHelper.push(remote, branch) }
    }

    fun pull(remote: String = "origin", branch: String = "main") {
        viewModelScope.launch { gitHelper.pull(remote, branch) }
    }

    fun fetch(remote: String = "origin") {
        viewModelScope.launch { gitHelper.fetch(remote) }
    }

    fun addRemote(name: String, url: String) {
        viewModelScope.launch { gitHelper.addRemote(name, url) }
    }

    fun listRemotes() {
        viewModelScope.launch { gitHelper.listRemotes() }
    }

    fun syncKeystoreCredentials(username: String, token: String) {
        viewModelScope.launch { gitHelper.configureKeystoreCredentials(username, token) }
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

