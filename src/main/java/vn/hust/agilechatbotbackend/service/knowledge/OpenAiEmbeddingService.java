package vn.hust.agilechatbotbackend.service.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible embedding service using text-embedding-3-small model.
 * Supports any OpenAI-compatible API (OpenAI, Azure OpenAI, local proxies).
 */
@Service
@Slf4j
public class OpenAiEmbeddingService implements EmbeddingService {

    private static final int EMBEDDING_DIMENSION = 1536;

    private final WebClient webClient;
    private final String model;

    public OpenAiEmbeddingService(
            @Value("${caretalk.embedding.api-key:}") String apiKey,
            @Value("${caretalk.embedding.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${caretalk.embedding.model:text-embedding-3-small}") String model) {

        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();

        log.info("Initialized OpenAI EmbeddingService with model={}, baseUrl={}", model, baseUrl);
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            log.warn("Attempted to embed empty text, returning zero vector");
            return new float[EMBEDDING_DIMENSION];
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "input", text,
                    "model", model
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/embeddings")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("data")) {
                log.error("Invalid embedding API response: {}", response);
                throw new RuntimeException("Invalid embedding API response");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            @SuppressWarnings("unchecked")
            List<Number> embeddingList = (List<Number>) data.get(0).get("embedding");

            float[] embedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embedding[i] = embeddingList.get(i).floatValue();
            }

            log.debug("Generated embedding for text (length={}) → vector dim={}", text.length(), embedding.length);
            return embedding;

        } catch (Exception e) {
            log.error("Failed to generate embedding for text: {}", e.getMessage(), e);
            throw new RuntimeException("Embedding generation failed", e);
        }
    }

    @Override
    public int getDimension() {
        return EMBEDDING_DIMENSION;
    }
}
