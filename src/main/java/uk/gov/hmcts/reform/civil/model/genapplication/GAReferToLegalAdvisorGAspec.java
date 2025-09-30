package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
@Builder(toBuilder = true)
public class GAReferToLegalAdvisorGAspec {

    private String legalAdvisorEventDescription;
    private String legalAdvisorAdditionalInfo;

    @JsonCreator
    GAReferToLegalAdvisorGAspec(@JsonProperty("legalAdvisorEventDescription") String legalAdvisorEventDescription,
                         @JsonProperty("legalAdvisorAdditionalInfo") String  legalAdvisorAdditionalInfo) {

        this.legalAdvisorEventDescription = legalAdvisorEventDescription;
        this.legalAdvisorAdditionalInfo = legalAdvisorAdditionalInfo;
    }
}
