package vn.hust.agilechatbotbackend.service.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import vn.hust.agilechatbotbackend.dto.LlmRequest;

import java.util.List;

/**
 * Routes LLM requests to the appropriate provider based on model name.
 * Acts as a facade over multiple LlmClient implementations.
 */
@Service
@Slf4j
public class LlmRouter {

    private final List<LlmClient> clients;

    public LlmRouter(List<LlmClient> clients) {
        this.clients = clients;
        log.info("Initialized LLM Router with {} providers: {}",
                clients.size(),
                clients.stream().map(LlmClient::getProviderName).toList());
    }

    /**
     * Route a request to the correct LlmClient based on the model name.
     * Falls back to the first available client if no specific match found.
     */
    public Flux<String> streamChat(LlmRequest request) {
        LlmClient client = resolveClient(request.getModel());
        log.debug("Routing model '{}' to provider '{}'", request.getModel(), client.getProviderName());
        return client.streamChat(request);
    }

    /**
     * Find the LlmClient that supports the given model.
     */
    private LlmClient resolveClient(String modelName) {
        return clients.stream()
                .filter(c -> c.supportsModel(modelName))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("No specific provider found for model '{}'. Using first available: {}",
                            modelName, clients.get(0).getProviderName());
                    return clients.get(0);
                });
    }
}
