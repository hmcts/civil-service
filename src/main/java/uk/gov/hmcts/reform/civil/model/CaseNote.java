package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CaseNote {

    private final String createdBy;
    private final LocalDateTime createdOn;
    private final String note;
}
