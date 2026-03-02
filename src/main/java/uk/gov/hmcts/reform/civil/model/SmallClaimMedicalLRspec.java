package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
public class SmallClaimMedicalLRspec {

    private final YesOrNo hasAgreedFreeMediation;

    @JsonCreator
    public SmallClaimMedicalLRspec(@JsonProperty("hasAgreedFreeMediation") YesOrNo hasAgreedFreeMediation) {
        this.hasAgreedFreeMediation = hasAgreedFreeMediation;
    }
}
