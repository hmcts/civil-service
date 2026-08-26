package uk.gov.hmcts.reform.civil.exceptions;

import lombok.Getter;

@Getter
public class UpstreamUnavailableException extends RuntimeException {

    private static final String ERROR_MESSAGE = "%s is currently unavailable";

    private final String upstreamService;
    private final String caseId;
    private final String userId;

    public UpstreamUnavailableException(String upstreamService, String caseId, String userId, Throwable cause) {
        super(String.format(ERROR_MESSAGE, upstreamService), cause);
        this.upstreamService = upstreamService;
        this.caseId = caseId;
        this.userId = userId;
    }

}
