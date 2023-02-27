package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class EventTemplateData {

    private LocalDate date;
    private String explanation;
}
