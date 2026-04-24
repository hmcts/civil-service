package uk.gov.hmcts.reform.civil.scheduler.common;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ErrorCategorizer {

    public String categorizeError(Exception e) {
        String message = Optional.ofNullable(e.getMessage()).orElse("").toLowerCase();
        if (message.contains("lock") || message.contains("conflict")) {
            return "lock conflict";
        }
        if (message.contains("idam") || message.contains("timeout")) {
            return "IDAM timeout";
        }
        if (e.getClass().getName().contains("FeignException") || message.contains("ccd")) {
            return "CCD error";
        }
        return "Other";
    }
}
