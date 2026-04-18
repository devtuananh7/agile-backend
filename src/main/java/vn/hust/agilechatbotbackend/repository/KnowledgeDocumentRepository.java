package vn.hust.agilechatbotbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hust.agilechatbotbackend.entity.KnowledgeDocument;

import java.util.List;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByCategoryAndIsActiveTrue(String category);

    List<KnowledgeDocument> findByIsActiveTrue();

    /**
     * Semantic similarity search using pgvector cosine distance.
     * Returns top-K documents with cosine similarity > threshold.
     * Note: pgvector uses cosine DISTANCE (1 - similarity), so we filter by distance < (1 - threshold).
     */
    @Query(value = """
            SELECT * FROM knowledge_documents
            WHERE is_active = true
              AND embedding IS NOT NULL
              AND (1 - (embedding <=> CAST(:queryVector AS vector))) > :threshold
            ORDER BY embedding <=> CAST(:queryVector AS vector)
            LIMIT :topK
            """, nativeQuery = true)
    List<KnowledgeDocument> findSimilarDocuments(@Param("queryVector") String queryVector,
                                                  @Param("threshold") double threshold,
                                                  @Param("topK") int topK);
}
