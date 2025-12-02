package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CaseNote {

    private String createdBy;
    private LocalDateTime createdOn;
    private String note;
}
