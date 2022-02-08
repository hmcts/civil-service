package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.CaseLink;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class GeneralApplicationDetails implements MappableObject {

    private final GAApplicationType generalAppType;
    private final GAApplicationState generalAppStatus;
    private final LocalDateTime submittedDate;
    private CaseLink caselink;

    @JsonCreator
    GeneralApplicationDetails(@JsonProperty("generalAppType") GAApplicationType generalAppType,
                              @JsonProperty("generalAppState") GAApplicationState generalAppStatus,
                              @JsonProperty("generalAppSubmittedDate") LocalDateTime submittedDate,
                              @JsonProperty("caseLink") CaseLink caseLink) {
        this.generalAppType = generalAppType;
        this.generalAppStatus = generalAppStatus;
        this.submittedDate = submittedDate;
        this.caselink = caseLink;
    }
}
