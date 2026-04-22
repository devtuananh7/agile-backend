package vn.hust.agilechatbotbackend.dto.metadata;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

// We use an interface with Jackson annotations so when Jackson deserializes JSONB
// into UserMetadata, it knows which implementation to choose based on some hint.
// Alternatively, since JSONB in DB might just be dumped by Jackson, we can let 
// users parse it on the fly, but having @JsonTypeInfo helps if stored natively.
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    defaultImpl = PatientMetadataDto.class
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PatientMetadataDto.class, name = "PATIENT"),
    @JsonSubTypes.Type(value = DoctorMetadataDto.class, name = "DOCTOR"),
    @JsonSubTypes.Type(value = AdminMetadataDto.class, name = "ADMIN")
})
public interface UserMetadata extends Serializable {
}
