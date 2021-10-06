package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolicitorReferences {

    private final String applicantSolicitor1Reference;
    private final String respondentSolicitor1Reference;
}
