package vn.hust.agilechatbotbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables asynchronous method execution for @Async annotated methods.
 * Used by SummaryGenerator to run summary generation without blocking chat responses.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
