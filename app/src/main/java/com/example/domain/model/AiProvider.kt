package com.example.domain.model

enum class AiProvider(
    val displayName: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val requiresApiKey: Boolean = true
) {
    GEMINI("Google Gemini", "https://generativelanguage.googleapis.com/v1beta/", "gemini-2.5-flash"),
    OPENAI("OpenAI", "https://api.openai.com/v1/", "gpt-4o"),
    CLAUDE("Anthropic Claude", "https://api.anthropic.com/v1/", "claude-3-7-sonnet"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1/", "deepseek-chat"),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1/", "anthropic/claude-3.5-sonnet"),
    GROQ("Groq", "https://api.groq.com/openai/v1/", "llama-3.3-70b-versatile"),
    MISTRAL("Mistral AI", "https://api.mistral.ai/v1/", "mistral-large-latest"),
    GLM("Zhipu GLM", "https://open.bigmodel.cn/api/paas/v4/", "glm-4"),
    KIMI("Moonshot Kimi", "https://api.moonshot.cn/v1/", "moonshot-v1-8k")
}

data class AiModel(
    val id: String,
    val name: String,
    val provider: AiProvider,
    val description: String = ""
) {
    companion object {
        fun getDefaultModels(): List<AiModel> = listOf(
            AiModel("gemini-2.5-flash", "Gemini 2.5 Flash", AiProvider.GEMINI, "Fast & intelligent multi-modal model"),
            AiModel("gemini-2.5-pro", "Gemini 2.5 Pro", AiProvider.GEMINI, "Reasoning & complex coding model"),
            AiModel("gpt-4o", "GPT-4o", AiProvider.OPENAI, "OpenAI flagship model"),
            AiModel("gpt-4o-mini", "GPT-4o Mini", AiProvider.OPENAI, "Fast lightweight model"),
            AiModel("claude-3-7-sonnet", "Claude 3.7 Sonnet", AiProvider.CLAUDE, "Hybrid reasoning & coding state of the art"),
            AiModel("claude-3-5-haiku", "Claude 3.5 Haiku", AiProvider.CLAUDE, "Ultra fast response model"),
            AiModel("deepseek-chat", "DeepSeek V3", AiProvider.DEEPSEEK, "High performance open weights powerhouse"),
            AiModel("deepseek-reasoner", "DeepSeek R1", AiProvider.DEEPSEEK, "Deep reasoning step-by-step model"),
            AiModel("anthropic/claude-3.5-sonnet", "Claude 3.5 Sonnet (OpenRouter)", AiProvider.OPENROUTER, "OpenRouter route to Claude"),
            AiModel("meta-llama/llama-3.3-70b-instruct", "Llama 3.3 70B (OpenRouter)", AiProvider.OPENROUTER, "OpenRouter Llama 3"),
            AiModel("deepseek/deepseek-r1", "DeepSeek R1 (OpenRouter)", AiProvider.OPENROUTER, "OpenRouter DeepSeek R1"),
            AiModel("llama-3.3-70b-versatile", "Llama 3.3 70B (Groq)", AiProvider.GROQ, "Ultra low-latency LMM"),
            AiModel("mistral-large-latest", "Mistral Large", AiProvider.MISTRAL, "Mistral flagship model"),
            AiModel("glm-4", "GLM-4", AiProvider.GLM, "Zhipu AI flagship model"),
            AiModel("moonshot-v1-8k", "Kimi 8K", AiProvider.KIMI, "Moonshot long context model")
        )
    }
}
