package uk.gov.hmcts.reform.civil.scheduler.common;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ErrorCategorizer {

    public String categorizeError(Throwable e) {
        String message = Optional.ofNullable(e.getMessage()).orElse("").toLowerCase();
        if (message.contains("lock") || message.contains("conflict") || message.contains("concurrency")) {
            return "Lock conflict";
        }
        if (message.contains("idam")) {
            return "IDAM error";
        }
        if (message.contains("timeout")) {
            return "Timeout error";
        }
        if (message.contains("ccd")) {
            return "CCD error";
        }
        return "Other";
    }
}
