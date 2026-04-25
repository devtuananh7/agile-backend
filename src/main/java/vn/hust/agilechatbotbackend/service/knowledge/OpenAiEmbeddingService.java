package vn.hust.agilechatbotbackend.service.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
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

    /** Maximum number of texts per batch API call to stay within OpenAI limits. */
    private static final int MAX_BATCH_SIZE = 100;

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
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        // Filter out null/blank texts, track their positions to insert zero vectors later
        List<Integer> validIndices = new ArrayList<>();
        List<String> validTexts = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            if (text != null && !text.isBlank()) {
                validIndices.add(i);
                validTexts.add(text);
            }
        }

        if (validTexts.isEmpty()) {
            // All texts were null/blank
            List<float[]> results = new ArrayList<>();
            for (int i = 0; i < texts.size(); i++) {
                results.add(new float[EMBEDDING_DIMENSION]);
            }
            return results;
        }

        // Embed valid texts in sub-batches
        List<float[]> validEmbeddings = new ArrayList<>();
        for (int batchStart = 0; batchStart < validTexts.size(); batchStart += MAX_BATCH_SIZE) {
            int batchEnd = Math.min(batchStart + MAX_BATCH_SIZE, validTexts.size());
            List<String> subBatch = validTexts.subList(batchStart, batchEnd);
            List<float[]> subResult = callBatchApi(subBatch);
            validEmbeddings.addAll(subResult);
        }

        // Reconstruct full results with zero vectors for null/blank texts
        float[][] allResults = new float[texts.size()][];
        for (int i = 0; i < texts.size(); i++) {
            allResults[i] = new float[EMBEDDING_DIMENSION]; // default zero vector
        }
        for (int i = 0; i < validIndices.size(); i++) {
            allResults[validIndices.get(i)] = validEmbeddings.get(i);
        }

        List<float[]> result = new ArrayList<>(texts.size());
        for (float[] arr : allResults) {
            result.add(arr);
        }

        log.info("Batch embedded {} texts ({} valid, {} blank/null)",
                texts.size(), validTexts.size(), texts.size() - validTexts.size());
        return result;
    }

    /**
     * Call OpenAI embeddings API with a batch of texts (max 100).
     */
    @SuppressWarnings("unchecked")
    private List<float[]> callBatchApi(List<String> texts) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "input", texts,
                    "model", model
            );

            Map<String, Object> response = webClient.post()
                    .uri("/embeddings")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("data")) {
                log.error("Invalid batch embedding API response: {}", response);
                throw new RuntimeException("Invalid batch embedding API response");
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");

            // Sort by index to ensure order matches input
            data.sort((a, b) -> ((Number) a.get("index")).intValue() - ((Number) b.get("index")).intValue());

            List<float[]> embeddings = new ArrayList<>(data.size());
            for (Map<String, Object> item : data) {
                List<Number> embeddingList = (List<Number>) item.get("embedding");
                float[] embedding = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    embedding[i] = embeddingList.get(i).floatValue();
                }
                embeddings.add(embedding);
            }

            log.debug("Batch API call: {} texts → {} embeddings", texts.size(), embeddings.size());
            return embeddings;

        } catch (Exception e) {
            log.error("Batch embedding API call failed for {} texts: {}", texts.size(), e.getMessage(), e);
            throw new RuntimeException("Batch embedding generation failed", e);
        }
    }

    @Override
    public int getDimension() {
        return EMBEDDING_DIMENSION;
    }
}

