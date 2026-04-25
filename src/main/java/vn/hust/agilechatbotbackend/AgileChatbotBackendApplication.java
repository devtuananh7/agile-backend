package vn.hust.agilechatbotbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgileChatbotBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgileChatbotBackendApplication.class, args);
    }

}
