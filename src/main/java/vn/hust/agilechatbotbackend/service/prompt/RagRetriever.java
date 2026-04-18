package vn.hust.agilechatbotbackend.service.prompt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.hust.agilechatbotbackend.entity.KnowledgeDocument;
import vn.hust.agilechatbotbackend.service.knowledge.KnowledgeService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Retrieves relevant knowledge documents via semantic search
 * and formats them as a [MEDICAL KNOWLEDGE] context section for the LLM prompt.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagRetriever {

    private static final double DEFAULT_THRESHOLD = 0.75;
    private static final int DEFAULT_TOP_K = 3;

    private final KnowledgeService knowledgeService;

    /**
     * Retrieve and format RAG context for a user message.
     *
     * @param userMessage the user's message text
     * @return formatted RAG context string, or null if no relevant documents found
     */
    public String retrieve(String userMessage) {
        return retrieve(userMessage, DEFAULT_THRESHOLD, DEFAULT_TOP_K);
    }

    /**
     * Retrieve and format RAG context with custom parameters.
     */
    public String retrieve(String userMessage, double threshold, int topK) {
        if (userMessage == null || userMessage.isBlank()) {
            return null;
        }

        try {
            List<KnowledgeDocument> documents = knowledgeService
                    .searchByText(userMessage, threshold, topK);

            if (documents.isEmpty()) {
                log.debug("No relevant documents found for RAG (threshold={})", threshold);
                return null;
            }

            String formatted = formatDocuments(documents);
            log.debug("RAG retrieved {} documents for context", documents.size());
            return formatted;

        } catch (Exception e) {
            log.error("RAG retrieval failed, proceeding without RAG context: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Format retrieved documents into a [MEDICAL KNOWLEDGE] section.
     */
    private String formatDocuments(List<KnowledgeDocument> documents) {
        StringBuilder sb = new StringBuilder();
        sb.append("[MEDICAL KNOWLEDGE - Sử dụng thông tin này để hỗ trợ tư vấn, không trích dẫn trực tiếp]\n\n");

        for (int i = 0; i < documents.size(); i++) {
            KnowledgeDocument doc = documents.get(i);
            sb.append("Tài liệu ").append(i + 1).append(": ").append(doc.getTitle()).append("\n");
            sb.append(doc.getContent()).append("\n\n");
        }

        return sb.toString().trim();
    }
}
