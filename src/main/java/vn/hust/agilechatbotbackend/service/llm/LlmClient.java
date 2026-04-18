package vn.hust.agilechatbotbackend.service.llm;

import reactor.core.publisher.Flux;
import vn.hust.agilechatbotbackend.dto.LlmRequest;

/**
 * Abstraction for LLM providers.
 * Implementations handle provider-specific API details (OpenAI, Gemini, etc).
 */
public interface LlmClient {

    /**
     * Stream a chat completion response.
     *
     * @param request the assembled LLM request
     * @return Flux of response text chunks (for SSE streaming)
     */
    Flux<String> streamChat(LlmRequest request);

    /**
     * Get the provider name this client handles.
     */
    String getProviderName();

    /**
     * Check if this client supports the given model name.
     */
    boolean supportsModel(String modelName);
}
