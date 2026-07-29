package com.example.utils

import com.example.data.repository.TerminalManager

class GitHelper(private val terminalManager: TerminalManager) {
    suspend fun status() {
        terminalManager.executeCommand("git status")
    }

    suspend fun clone(repoUrl: String) {
        terminalManager.executeCommand("git clone $repoUrl")
    }

    suspend fun addAll() {
        terminalManager.executeCommand("git add .")
    }

    suspend fun commit(message: String) {
        terminalManager.executeCommand("git commit -m \"$message\"")
    }

    suspend fun push(remote: String = "origin", branch: String = "main") {
        terminalManager.executeCommand("git push $remote $branch")
    }

    suspend fun pull(remote: String = "origin", branch: String = "main") {
        terminalManager.executeCommand("git pull $remote $branch")
    }

    suspend fun fetch(remote: String = "origin") {
        terminalManager.executeCommand("git fetch $remote")
    }

    suspend fun addRemote(name: String, url: String) {
        terminalManager.executeCommand("git remote add $name $url || git remote set-url $name $url")
    }

    suspend fun listRemotes() {
        terminalManager.executeCommand("git remote -v")
    }

    suspend fun configureKeystoreCredentials(username: String, token: String) {
        if (username.isNotBlank()) {
            terminalManager.executeCommand("git config user.name \"$username\"")
        }
        if (token.isNotBlank()) {
            terminalManager.executeCommand("git config credential.helper store")
        }
    }

    suspend fun log() {
        terminalManager.executeCommand("git log -n 20 --oneline")
    }

    suspend fun diff() {
        terminalManager.executeCommand("git diff")
    }

    suspend fun listBranches() {
        terminalManager.executeCommand("git branch -a")
    }
}

