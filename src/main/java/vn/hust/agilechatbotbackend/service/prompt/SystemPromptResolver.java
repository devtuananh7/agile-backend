package vn.hust.agilechatbotbackend.service.prompt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.hust.agilechatbotbackend.entity.SystemPrompt;
import vn.hust.agilechatbotbackend.repository.SystemPromptRepository;

import java.util.Optional;

/**
 * Resolves system prompts from the database by name.
 * Falls back to a hardcoded default prompt if no matching record is found.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemPromptResolver {

    private static final String DEFAULT_PROMPT_NAME = "medical_general";

    private static final String DEFAULT_FALLBACK_PROMPT = """
            Bạn là trợ lý y tế CareTalk. Nhiệm vụ của bạn là:
            - Tư vấn sơ bộ dựa trên triệu chứng mà người dùng mô tả
            - Đặt câu hỏi để thu thập thêm thông tin y tế
            - Không đưa ra chẩn đoán chính thức
            - Khuyến khích người dùng đi khám bác sĩ khi triệu chứng nghiêm trọng
            - Trả lời bằng tiếng Việt, rõ ràng và dễ hiểu
            - Luôn ưu tiên an toàn sức khỏe của người dùng
            """;

    private static final String DEFAULT_MODEL = "gpt-4o";
    private static final double DEFAULT_TEMPERATURE = 0.3;

    private final SystemPromptRepository systemPromptRepository;

    /**
     * Resolve system prompt by name.
     * Falls back to default hardcoded prompt if not found in DB.
     */
    public SystemPrompt resolveByName(String promptName) {
        String name = (promptName != null && !promptName.isBlank()) ? promptName : DEFAULT_PROMPT_NAME;

        Optional<SystemPrompt> found = systemPromptRepository.findByNameAndIsActiveTrue(name);

        if (found.isPresent()) {
            log.debug("Resolved system prompt '{}' from database", name);
            return found.get();
        }

        // Fallback: try default prompt name if different from requested
        if (!DEFAULT_PROMPT_NAME.equals(name)) {
            Optional<SystemPrompt> defaultPrompt = systemPromptRepository.findByNameAndIsActiveTrue(DEFAULT_PROMPT_NAME);
            if (defaultPrompt.isPresent()) {
                log.warn("System prompt '{}' not found. Falling back to '{}'", name, DEFAULT_PROMPT_NAME);
                return defaultPrompt.get();
            }
        }

        // Final fallback: hardcoded default
        log.warn("No system prompt found in database for '{}'. Using hardcoded fallback.", name);
        return buildFallbackPrompt();
    }

    /**
     * Build a fallback SystemPrompt object with hardcoded defaults.
     */
    private SystemPrompt buildFallbackPrompt() {
        return SystemPrompt.builder()
                .name(DEFAULT_PROMPT_NAME)
                .content(DEFAULT_FALLBACK_PROMPT)
                .version(0)
                .isActive(true)
                .metadata(java.util.Map.of(
                        "model", DEFAULT_MODEL,
                        "temperature", DEFAULT_TEMPERATURE
                ))
                .build();
    }

    /**
     * Get the model name from a resolved system prompt.
     */
    public String getModelName(SystemPrompt prompt) {
        return prompt.getModelName();
    }

    /**
     * Get the temperature from a resolved system prompt.
     */
    public double getTemperature(SystemPrompt prompt) {
        return prompt.getTemperature();
    }
}
