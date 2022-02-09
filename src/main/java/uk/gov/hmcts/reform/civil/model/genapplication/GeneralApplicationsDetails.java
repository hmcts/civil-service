package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class GeneralApplicationsDetails implements MappableObject {

    private final String generalAppType1;
    private final LocalDateTime generalAppSubmittedDateGAspec;
    private CaseLink caseLink;
    private final CaseState caseState;

    @JsonCreator
    GeneralApplicationsDetails(@JsonProperty("generalAppType1") String generalAppType1,
                               @JsonProperty("generalAppSubmittedDateGAspec") LocalDateTime generalAppSubmittedDateGAspec,
                               @JsonProperty("caseLink") CaseLink caseLink,
                               @JsonProperty("caseState") CaseState caseState) {
        this.generalAppType1 = generalAppType1;
        this.generalAppSubmittedDateGAspec = generalAppSubmittedDateGAspec;
        this.caseLink = caseLink;
        this.caseState = caseState;
    }
}
