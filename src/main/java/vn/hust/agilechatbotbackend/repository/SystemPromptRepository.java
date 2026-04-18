package vn.hust.agilechatbotbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hust.agilechatbotbackend.entity.SystemPrompt;

import java.util.Optional;

@Repository
public interface SystemPromptRepository extends JpaRepository<SystemPrompt, Long> {

    Optional<SystemPrompt> findByNameAndIsActiveTrue(String name);
}
