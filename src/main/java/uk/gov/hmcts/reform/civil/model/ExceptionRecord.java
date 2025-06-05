package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;

import java.util.List;

import static java.util.Collections.emptyList;

@Builder
public record ExceptionRecord(String taskId, String caseReference, List<String> successfulActions) {
    public ExceptionRecord(String taskId, String caseReference) {
        this(taskId, caseReference, emptyList());
    }
}
