package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
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
