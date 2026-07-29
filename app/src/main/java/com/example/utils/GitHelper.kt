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

    suspend fun push() {
        terminalManager.executeCommand("git push")
    }

    suspend fun pull() {
        terminalManager.executeCommand("git pull")
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
