package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class GeneralApplicationsDetails implements MappableObject {

    private final String generalApplicationType;
    private final LocalDateTime generalAppSubmittedDateGAspec;
    private CaseLink caseLink;
    private final String caseState;

    @JsonCreator
    GeneralApplicationsDetails(@JsonProperty("generalApplicationType") String generalApplicationType,
                               @JsonProperty("generalAppSubmittedDateGAspec") LocalDateTime generalAppSubmittedDateGAspec,
                               @JsonProperty("caseLink") CaseLink caseLink,
                               @JsonProperty("caseState") String caseState) {
        this.generalApplicationType = generalApplicationType;
        this.generalAppSubmittedDateGAspec = generalAppSubmittedDateGAspec;
        this.caseLink = caseLink;
        this.caseState = caseState;
    }
}
