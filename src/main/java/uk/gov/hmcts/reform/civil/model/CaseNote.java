package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CaseNote {

    private final String createdBy;
    private final LocalDate createdOn;
    private final String note;
}
