package vn.hust.agilechatbotbackend.service.knowledge;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for splitting long text content into smaller, overlapping chunks
 * suitable for embedding. Prefers splitting at natural boundaries
 * (paragraphs, sentences, words) rather than mid-word.
 */
@Component
public class TextChunker {

    /** Default maximum tokens per chunk. */
    public static final int DEFAULT_MAX_TOKENS = 800;

    /** Default overlap tokens between consecutive chunks. */
    public static final int DEFAULT_OVERLAP_TOKENS = 100;

    /**
     * Approximate tokens-per-word ratio for Vietnamese text.
     * Vietnamese words tend to tokenize at ~0.75 tokens/word (conservative).
     */
    private static final double TOKENS_PER_WORD = 0.75;

    /**
     * Split content into chunks with default parameters.
     */
    public List<String> chunk(String content) {
        return chunk(content, DEFAULT_MAX_TOKENS, DEFAULT_OVERLAP_TOKENS);
    }

    /**
     * Split content into chunks of at most {@code maxTokens} tokens,
     * with {@code overlapTokens} tokens of overlap between consecutive chunks.
     *
     * @param content       the text to split
     * @param maxTokens     maximum token count per chunk
     * @param overlapTokens number of overlapping tokens between chunks
     * @return list of chunk strings; single-element list if content fits in one chunk
     */
    public List<String> chunk(String content, int maxTokens, int overlapTokens) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        int estimatedTokens = estimateTokens(content);
        if (estimatedTokens <= maxTokens) {
            return List.of(content.trim());
        }

        List<String> chunks = new ArrayList<>();
        int maxWords = tokensToWords(maxTokens);
        int overlapWords = tokensToWords(overlapTokens);

        String[] words = content.split("\\s+");
        int start = 0;

        while (start < words.length) {
            int end = Math.min(start + maxWords, words.length);

            // Try to find a natural break point near the end
            if (end < words.length) {
                end = findNaturalBreak(words, start, end, maxWords);
            }

            String chunk = joinWords(words, start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end >= words.length) {
                break;
            }

            // Move start forward, accounting for overlap
            int advance = end - start - overlapWords;
            if (advance <= 0) {
                // Prevent infinite loop: advance at least 1 word
                advance = 1;
            }
            start = start + advance;
        }

        return chunks;
    }

    /**
     * Estimate the number of tokens for the given text.
     */
    public int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        int wordCount = text.split("\\s+").length;
        return (int) Math.ceil(wordCount * TOKENS_PER_WORD);
    }

    /**
     * Find a natural break point near the target end position.
     * Priority: paragraph break > sentence boundary > word boundary.
     */
    private int findNaturalBreak(String[] words, int start, int targetEnd, int maxWords) {
        // Search window: look back up to 20% of maxWords for a natural break
        int searchBack = Math.max(1, maxWords / 5);
        int searchStart = Math.max(start + 1, targetEnd - searchBack);

        // Priority 1: paragraph break (word following "\n\n" or ending with "\n")
        for (int i = targetEnd - 1; i >= searchStart; i--) {
            if (words[i].endsWith("\n") || (i + 1 < words.length && isAfterParagraphBreak(words, i))) {
                return i + 1;
            }
        }

        // Priority 2: sentence boundary (word ending with . ! ?)
        for (int i = targetEnd - 1; i >= searchStart; i--) {
            String word = words[i];
            if (word.endsWith(".") || word.endsWith("!") || word.endsWith("?")
                    || word.endsWith(".)") || word.endsWith("?)") || word.endsWith("!)")
                    || word.endsWith(".\"") || word.endsWith("?\"") || word.endsWith("!\"")) {
                return i + 1;
            }
        }

        // Priority 3: fall back to word boundary at target position
        return targetEnd;
    }

    /**
     * Check if the text between words[i] and words[i+1] had a paragraph break.
     * Since we split on whitespace, we need to reconstruct the original gap.
     * This heuristic checks if the original content had double newlines.
     */
    private boolean isAfterParagraphBreak(String[] words, int i) {
        // In the split array, paragraph breaks are lost.
        // We check if the word itself contains newlines as a heuristic.
        return words[i].contains("\n\n");
    }

    /**
     * Join words from start (inclusive) to end (exclusive).
     */
    private String joinWords(String[] words, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (i > start) sb.append(" ");
            sb.append(words[i]);
        }
        return sb.toString();
    }

    /**
     * Convert token count to approximate word count.
     */
    private int tokensToWords(int tokens) {
        return Math.max(1, (int) Math.ceil(tokens / TOKENS_PER_WORD));
    }
}
