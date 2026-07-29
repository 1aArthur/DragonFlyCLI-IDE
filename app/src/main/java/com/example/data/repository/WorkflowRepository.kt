package com.example.data.repository

import com.example.data.db.dao.WorkflowDao
import com.example.data.db.entities.WorkflowEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class WorkflowRepository(
    private val workflowDao: WorkflowDao,
    private val terminalManager: TerminalManager
) {
    val workflows: Flow<List<WorkflowEntity>> = workflowDao.getAllWorkflows()

    suspend fun seedDefaultMacrosIfNeeded() {
        val current = workflowDao.getAllWorkflows().first()
        if (current.isEmpty()) {
            val defaultMacros = listOf(
                WorkflowEntity(
                    id = "macro_build_compile",
                    name = "Compilação & Gradle Build",
                    description = "Verifica integridade da aplicação e executa build Gradle de depuração.",
                    commands = "echo '== Iniciando Build de Compilação =='; pwd; gradle assembleDebug",
                    createdAt = System.currentTimeMillis()
                ),
                WorkflowEntity(
                    id = "macro_file_cleanup",
                    name = "Limpeza de Cache e Temp",
                    description = "Remove arquivos temporários, logs antigos e limpa workspace.",
                    commands = "echo '== Executando Limpeza de Arquivos =='; rm -f *.tmp; rm -f *.log; ls -la",
                    createdAt = System.currentTimeMillis() - 1000
                ),
                WorkflowEntity(
                    id = "macro_git_quick_commit",
                    name = "Git Quick Backup Commit",
                    description = "Adiciona alterações pendentes e cria um commit automático com timestamp.",
                    commands = "echo '== Backup Git Automático =='; git status; git add .; git commit -m 'Auto-backup via DragonflyCLI Dashboard'",
                    createdAt = System.currentTimeMillis() - 2000
                ),
                WorkflowEntity(
                    id = "macro_termux_diagnostics",
                    name = "Diagnóstico Termux:API",
                    description = "Testa conexão com o bridge Termux, status da bateria e notificação toast.",
                    commands = "echo '== Diagnóstico Termux:API =='; termux-battery-status; termux-toast 'DragonflyCLI Automation Active'",
                    createdAt = System.currentTimeMillis() - 3000
                )
            )
            defaultMacros.forEach { workflowDao.insertWorkflow(it) }
        }
    }

    suspend fun createWorkflow(name: String, description: String, commands: String): WorkflowEntity {
        val entity = WorkflowEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            commands = commands,
            createdAt = System.currentTimeMillis()
        )
        workflowDao.insertWorkflow(entity)
        return entity
    }

    suspend fun executeWorkflow(workflow: WorkflowEntity, onStepLog: ((String) -> Unit)? = null) {
        val cmdList = workflow.commands.split("\n", ";").map { it.trim() }.filter { it.isNotEmpty() }
        onStepLog?.invoke("▶ Iniciando Workflow: ${workflow.name} (${cmdList.size} etapas)")
        for ((index, cmd) in cmdList.withIndex()) {
            onStepLog?.invoke("  [${index + 1}/${cmdList.size}] Executando: $cmd")
            terminalManager.executeCommand(cmd)
        }
        onStepLog?.invoke("✔ Workflow '${workflow.name}' concluído com sucesso.")
    }

    suspend fun deleteWorkflow(workflow: WorkflowEntity) {
        workflowDao.deleteWorkflow(workflow)
    }
}

