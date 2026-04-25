package vn.hust.agilechatbotbackend.service.knowledge;

import java.util.List;

/**
 * Interface for generating embedding vectors from text.
 * Implementations may use different embedding providers (OpenAI, etc).
 */
public interface EmbeddingService {

    /**
     * Generate an embedding vector from the given text.
     *
     * @param text the text to embed
     * @return float array representing the embedding vector (e.g., 1536 dimensions)
     */
    float[] embed(String text);

    /**
     * Generate embedding vectors for multiple texts in a batch.
     * More efficient than calling embed() individually for each text.
     *
     * @param texts the list of texts to embed
     * @return list of float arrays, one per input text, in the same order
     */
    List<float[]> embedBatch(List<String> texts);

    /**
     * Get the dimensionality of the embedding vectors produced by this service.
     */
    int getDimension();
}

