package vn.hust.agilechatbotbackend.service.knowledge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.agilechatbotbackend.entity.KnowledgeDocument;
import vn.hust.agilechatbotbackend.repository.KnowledgeDocumentRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing knowledge documents and performing semantic search.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final EmbeddingService embeddingService;

    /**
     * Save a new knowledge document with auto-generated embedding.
     */
    @Transactional
    public KnowledgeDocument saveDocument(String title, String content, String category, List<String> tags) {
        float[] embedding = embeddingService.embed(content);

        KnowledgeDocument document = KnowledgeDocument.builder()
                .title(title)
                .content(content)
                .category(category)
                .tags(tags)
                .embedding(embedding)
                .isActive(true)
                .build();

        KnowledgeDocument saved = knowledgeDocumentRepository.save(document);
        log.info("Saved knowledge document id={} title='{}' category='{}'",
                saved.getId(), title, category);
        return saved;
    }

    /**
     * Search for semantically similar documents using pgvector cosine similarity.
     *
     * @param queryVector the query embedding vector
     * @param threshold   minimum cosine similarity (0.0 to 1.0), default 0.75
     * @param topK        maximum number of results, default 3
     * @return list of matching documents ordered by similarity descending
     */
    public List<KnowledgeDocument> searchSimilar(float[] queryVector, double threshold, int topK) {
        // Convert float array to pgvector-compatible string format: [0.1,0.2,0.3,...]
        String vectorString = floatArrayToVectorString(queryVector);

        List<KnowledgeDocument> results = knowledgeDocumentRepository
                .findSimilarDocuments(vectorString, threshold, topK);

        log.debug("Semantic search returned {} results (threshold={}, topK={})",
                results.size(), threshold, topK);
        return results;
    }

    /**
     * Search for similar documents by embedding a query text first.
     */
    public List<KnowledgeDocument> searchByText(String queryText, double threshold, int topK) {
        float[] queryVector = embeddingService.embed(queryText);
        return searchSimilar(queryVector, threshold, topK);
    }

    /**
     * Search with default parameters (threshold=0.75, topK=3).
     */
    public List<KnowledgeDocument> searchByText(String queryText) {
        return searchByText(queryText, 0.75, 3);
    }

    /**
     * Convert float array to pgvector string format for native query.
     * Example: [0.123, -0.456, 0.789]
     */
    private String floatArrayToVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
