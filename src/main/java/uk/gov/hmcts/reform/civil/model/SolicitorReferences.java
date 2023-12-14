package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolicitorReferences {

    private String applicantSolicitor1Reference;
    private String respondentSolicitor1Reference;
    private String respondentSolicitor2Reference;
}
