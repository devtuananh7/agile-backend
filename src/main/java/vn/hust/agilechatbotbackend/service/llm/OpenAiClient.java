package vn.hust.agilechatbotbackend.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import vn.hust.agilechatbotbackend.dto.LlmRequest;
import vn.hust.agilechatbotbackend.service.prompt.PromptAssembler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OpenAI-compatible LLM client.
 * Supports streaming via Server-Sent Events (SSE).
 * Works with OpenAI API and compatible providers (Azure, local proxies).
 */
@Service
@Slf4j
public class OpenAiClient implements LlmClient {

    private static final Set<String> SUPPORTED_MODELS = Set.of(
            "gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo",
            "o1", "o1-mini", "o3-mini"
    );

    private final WebClient webClient;
    private final PromptAssembler promptAssembler;
    private final ObjectMapper objectMapper;

    public OpenAiClient(
            @Value("${caretalk.llm.openai.api-key:${caretalk.embedding.api-key:}}") String apiKey,
            @Value("${caretalk.llm.openai.base-url:https://api.openai.com/v1}") String baseUrl,
            PromptAssembler promptAssembler,
            ObjectMapper objectMapper) {

        this.promptAssembler = promptAssembler;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();

        log.info("Initialized OpenAI LLM client with baseUrl={}", baseUrl);
    }

    @Override
    public Flux<String> streamChat(LlmRequest request) {
        // Convert LlmRequest to OpenAI message format
        List<LlmRequest.ChatMessage> messages = promptAssembler.toOpenAiMessages(request);

        // Build request body
        List<Map<String, String>> messageList = messages.stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList());

        Map<String, Object> requestBody = Map.of(
                "model", request.getModel(),
                "temperature", request.getTemperature(),
                "messages", messageList,
                "stream", true
        );

        log.debug("Streaming OpenAI request: model={}, messages={}, temp={}",
                request.getModel(), messages.size(), request.getTemperature());

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(data -> !data.equals("[DONE]") && !data.isBlank())
                .mapNotNull(this::extractContent)
                .doOnError(e -> log.error("OpenAI streaming error: {}", e.getMessage()))
                .onErrorResume(e -> Flux.just("[Error: " + e.getMessage() + "]"));
    }

    /**
     * Extract content delta from SSE data chunk.
     * OpenAI format: {"choices":[{"delta":{"content":"text"}}]}
     */
    private String extractContent(String data) {
        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.get("choices");

            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                JsonNode delta = choices.get(0).get("delta");
                if (delta != null && delta.has("content")) {
                    return delta.get("content").asText();
                }
            }
            return null;
        } catch (Exception e) {
            log.trace("Failed to parse SSE chunk: {}", data);
            return null;
        }
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public boolean supportsModel(String modelName) {
        return modelName != null && SUPPORTED_MODELS.stream()
                .anyMatch(m -> modelName.toLowerCase().startsWith(m));
    }
}
