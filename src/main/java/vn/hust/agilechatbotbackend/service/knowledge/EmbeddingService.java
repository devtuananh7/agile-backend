package vn.hust.agilechatbotbackend.service.knowledge;

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
     * Get the dimensionality of the embedding vectors produced by this service.
     */
    int getDimension();
}
