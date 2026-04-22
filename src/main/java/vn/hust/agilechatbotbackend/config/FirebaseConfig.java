package vn.hust.agilechatbotbackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * Initialize FirebaseApp at startup.
 * Priority: FIREBASE_CREDENTIALS_BASE64 env var → firebase.credentials-file property.
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials-file:}")
    private String credentialsFile;

    @Value("${FIREBASE_CREDENTIALS_BASE64:}")
    private String credentialsBase64;

    @PostConstruct
    public void initFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            GoogleCredentials credentials = loadCredentials();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");
        } else {
            log.info("Firebase already initialized");
        }
    }

    private GoogleCredentials loadCredentials() throws IOException {
        // Priority 1: Base64-encoded env var (production)
        if (credentialsBase64 != null && !credentialsBase64.isBlank()) {
            log.info("Loading Firebase credentials from FIREBASE_CREDENTIALS_BASE64 env var");
            byte[] decoded = Base64.getDecoder().decode(credentialsBase64);
            InputStream stream = new ByteArrayInputStream(decoded);
            return GoogleCredentials.fromStream(stream);
        }

        // Priority 2: File path (development)
        if (credentialsFile != null && !credentialsFile.isBlank()) {
            log.info("Loading Firebase credentials from file: {}", credentialsFile);
            InputStream stream = new FileInputStream(credentialsFile);
            return GoogleCredentials.fromStream(stream);
        }

        throw new IllegalStateException(
                "Firebase credentials not configured. Set FIREBASE_CREDENTIALS_BASE64 env var " +
                "or firebase.credentials-file property.");
    }
}
