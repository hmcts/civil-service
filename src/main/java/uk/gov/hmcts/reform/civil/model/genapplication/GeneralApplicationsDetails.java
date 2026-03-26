package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GeneralApplicationsDetails implements MappableObject {

    private String generalApplicationType;
    private LocalDateTime generalAppSubmittedDateGAspec;
    private CaseLink caseLink;
    private String caseState;
    private YesOrNo parentClaimantIsApplicant;

    @JsonCreator
    GeneralApplicationsDetails(@JsonProperty("generalApplicationType") String generalApplicationType,
                               @JsonProperty("generalAppSubmittedDateGAspec")
                                   LocalDateTime generalAppSubmittedDateGAspec,
                               @JsonProperty("caseLink") CaseLink caseLink,
                               @JsonProperty("caseState") String caseState,
                               @JsonProperty("parentClaimantIsApplicant") YesOrNo parentClaimantIsApplicant) {
        this.generalApplicationType = generalApplicationType;
        this.generalAppSubmittedDateGAspec = generalAppSubmittedDateGAspec;
        this.caseLink = caseLink;
        this.caseState = caseState;
        this.parentClaimantIsApplicant = parentClaimantIsApplicant;
    }
}
