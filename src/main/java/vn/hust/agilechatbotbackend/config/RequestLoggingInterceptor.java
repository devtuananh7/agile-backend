package vn.hust.agilechatbotbackend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

/**
 * HandlerInterceptor that logs API request summary:
 * [API] METHOD URI | STATUS | DURATIONms | body: REQUEST_BODY
 *
 * preHandle: records start time
 * afterCompletion: calculates duration, reads cached body, logs summary
 */
@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "requestStartTime";
    private static final int MAX_BODY_LENGTH = 500;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        long startTime = (long) request.getAttribute(START_TIME_ATTR);
        long duration = System.currentTimeMillis() - startTime;

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        String body = extractBody(request);

        log.info("[API] {} {} | {} | {}ms | body: {}",
                method, uri, status, duration, body);
    }

    /**
     * Extract request body from ContentCachingRequestWrapper.
     * Truncates to MAX_BODY_LENGTH characters.
     */
    private String extractBody(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length == 0) {
                return "(empty)";
            }

            String body = new String(buf, StandardCharsets.UTF_8).replaceAll("\\s+", " ").trim();

            if (body.length() > MAX_BODY_LENGTH) {
                return body.substring(0, MAX_BODY_LENGTH) + "...[truncated]";
            }
            return body;
        }
        return "(not cached)";
    }
}
