package vn.hust.agilechatbotbackend.service.knowledge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.agilechatbotbackend.dto.BulkImportResponse;
import vn.hust.agilechatbotbackend.dto.KnowledgeDocumentRequest;
import vn.hust.agilechatbotbackend.entity.KnowledgeDocument;
import vn.hust.agilechatbotbackend.repository.KnowledgeDocumentRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing knowledge documents and performing semantic search.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final EmbeddingService embeddingService;
    private final TextChunker textChunker;

    /**
     * Save a new knowledge document with auto-generated embedding.
     * If the content exceeds the max token limit, it is automatically chunked.
     *
     * @return list of saved documents (1 if no chunking, N if chunked)
     * @throws IllegalArgumentException if a document with the same title already exists
     */
    @Transactional
    public List<KnowledgeDocument> saveDocument(String title, String content, String category, List<String> tags) {
        // Deduplication check
        if (knowledgeDocumentRepository.existsByTitleIgnoreCase(title)) {
            throw new IllegalArgumentException("Document with title '" + title + "' already exists");
        }

        // Check if chunking is needed
        List<String> chunks = textChunker.chunk(content);

        if (chunks.size() <= 1) {
            // No chunking needed — single document
            float[] embedding = embeddingService.embed(content);
            KnowledgeDocument document = buildDocument(title, content, category, tags, embedding);
            KnowledgeDocument saved = knowledgeDocumentRepository.save(document);
            log.info("Saved knowledge document id={} title='{}' category='{}'",
                    saved.getId(), title, category);
            return List.of(saved);
        }

        // Chunking needed — create multiple documents
        List<KnowledgeDocument> savedDocs = new ArrayList<>();
        List<float[]> embeddings = embeddingService.embedBatch(chunks);

        for (int i = 0; i < chunks.size(); i++) {
            String chunkTitle = title + " [Part " + (i + 1) + "]";
            KnowledgeDocument document = buildDocument(chunkTitle, chunks.get(i), category, tags, embeddings.get(i));
            KnowledgeDocument saved = knowledgeDocumentRepository.save(document);
            savedDocs.add(saved);
        }

        log.info("Saved chunked knowledge document '{}' → {} parts, category='{}'",
                title, savedDocs.size(), category);
        return savedDocs;
    }

    /**
     * Bulk import documents with chunking and batch embedding.
     */
    @Transactional
    public BulkImportResponse saveDocumentsBulk(List<KnowledgeDocumentRequest> requests) {
        int imported = 0;
        int failed = 0;
        List<BulkImportResponse.ImportError> errors = new ArrayList<>();

        // Phase 1: Validate and chunk all documents
        List<ChunkRecord> allChunks = new ArrayList<>();
        List<KnowledgeDocumentRequest> validRequests = new ArrayList<>();

        for (KnowledgeDocumentRequest request : requests) {
            // Dedup check
            if (knowledgeDocumentRepository.existsByTitleIgnoreCase(request.getTitle())) {
                failed++;
                errors.add(BulkImportResponse.ImportError.builder()
                        .title(request.getTitle())
                        .reason("Duplicate title")
                        .build());
                continue;
            }

            List<String> chunks = textChunker.chunk(request.getContent());
            if (chunks.size() <= 1) {
                allChunks.add(new ChunkRecord(request.getTitle(), request.getContent(),
                        request.getCategory(), request.getTags()));
            } else {
                for (int i = 0; i < chunks.size(); i++) {
                    String chunkTitle = request.getTitle() + " [Part " + (i + 1) + "]";
                    allChunks.add(new ChunkRecord(chunkTitle, chunks.get(i),
                            request.getCategory(), request.getTags()));
                }
            }
            validRequests.add(request);
        }

        if (allChunks.isEmpty()) {
            return BulkImportResponse.builder()
                    .imported(0)
                    .failed(failed)
                    .errors(errors)
                    .build();
        }

        // Phase 2: Batch embed all chunks
        List<String> allContents = allChunks.stream().map(ChunkRecord::content).toList();
        List<float[]> allEmbeddings = embeddingService.embedBatch(allContents);

        // Phase 3: Persist all documents
        for (int i = 0; i < allChunks.size(); i++) {
            ChunkRecord chunk = allChunks.get(i);
            KnowledgeDocument document = buildDocument(
                    chunk.title(), chunk.content(), chunk.category(), chunk.tags(), allEmbeddings.get(i));
            knowledgeDocumentRepository.save(document);
        }

        imported = validRequests.size();
        log.info("Bulk import complete: {} imported ({} chunks), {} failed",
                imported, allChunks.size(), failed);

        return BulkImportResponse.builder()
                .imported(imported)
                .failed(failed)
                .errors(errors)
                .build();
    }

    /**
     * Update an existing knowledge document.
     * Re-embeds only if content changes.
     */
    @Transactional
    public KnowledgeDocument updateDocument(Long id, String title, String content, String category, List<String> tags) {
        KnowledgeDocument existing = knowledgeDocumentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + id));

        boolean contentChanged = !existing.getContent().equals(content);

        existing.setTitle(title);
        existing.setContent(content);
        existing.setCategory(category);
        existing.setTags(tags);

        if (contentChanged) {
            float[] newEmbedding = embeddingService.embed(content);
            existing.setEmbedding(newEmbedding);
            log.info("Updated document id={} with re-embedding (content changed)", id);
        } else {
            log.info("Updated document id={} metadata only (content unchanged)", id);
        }

        return knowledgeDocumentRepository.save(existing);
    }

    /**
     * Soft delete a knowledge document.
     */
    @Transactional
    public void softDelete(Long id) {
        KnowledgeDocument document = knowledgeDocumentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + id));
        document.setIsActive(false);
        knowledgeDocumentRepository.save(document);
        log.info("Soft-deleted document id={} title='{}'", id, document.getTitle());
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
     * Search for similar documents filtered by category.
     */
    public List<KnowledgeDocument> searchByTextAndCategory(String queryText, String category,
                                                            double threshold, int topK) {
        float[] queryVector = embeddingService.embed(queryText);
        String vectorString = floatArrayToVectorString(queryVector);

        List<KnowledgeDocument> results = knowledgeDocumentRepository
                .findSimilarDocumentsByCategory(vectorString, category, threshold, topK);

        log.debug("Category-filtered semantic search returned {} results (category={}, threshold={}, topK={})",
                results.size(), category, threshold, topK);
        return results;
    }

    /**
     * List active documents with pagination.
     */
    public Page<KnowledgeDocument> listDocuments(Pageable pageable) {
        return knowledgeDocumentRepository.findByIsActiveTrue(pageable);
    }

    /**
     * List active documents filtered by category with pagination.
     */
    public Page<KnowledgeDocument> listDocumentsByCategory(String category, Pageable pageable) {
        return knowledgeDocumentRepository.findByCategoryAndIsActiveTrue(category, pageable);
    }

    /**
     * Find a document by ID.
     */
    public KnowledgeDocument findById(Long id) {
        return knowledgeDocumentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + id));
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

    private KnowledgeDocument buildDocument(String title, String content, String category,
                                             List<String> tags, float[] embedding) {
        return KnowledgeDocument.builder()
                .title(title)
                .content(content)
                .category(category)
                .tags(tags)
                .embedding(embedding)
                .isActive(true)
                .build();
    }

    /**
     * Internal record for tracking chunks during bulk import.
     */
    private record ChunkRecord(String title, String content, String category, List<String> tags) {}
}
