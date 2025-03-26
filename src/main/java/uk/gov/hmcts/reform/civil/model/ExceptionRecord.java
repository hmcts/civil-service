package uk.gov.hmcts.reform.civil.model;

import java.util.List;

public record ExceptionRecord(String taskId, String caseReference, List<String> successfulActions) {}
