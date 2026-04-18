package vn.hust.agilechatbotbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "system_prompts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Extract model name from metadata JSONB.
     * Falls back to default if not configured.
     */
    public String getModelName() {
        if (metadata != null && metadata.containsKey("model")) {
            return (String) metadata.get("model");
        }
        return "gpt-4o";
    }

    /**
     * Extract temperature from metadata JSONB.
     * Falls back to 0.3 (conservative for medical context).
     */
    public double getTemperature() {
        if (metadata != null && metadata.containsKey("temperature")) {
            Object temp = metadata.get("temperature");
            if (temp instanceof Number) {
                return ((Number) temp).doubleValue();
            }
        }
        return 0.3;
    }
}
