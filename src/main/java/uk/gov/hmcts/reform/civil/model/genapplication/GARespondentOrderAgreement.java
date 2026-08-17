package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "GARespondentAgreementGAspec", generate = true)
@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GARespondentOrderAgreement {

    @CCD(label = "A judge will still need to approve this order", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo hasAgreed;

    @JsonCreator
    GARespondentOrderAgreement(@JsonProperty("hasAgreed") YesOrNo hasAgreed) {
        this.hasAgreed = hasAgreed;
    }
}
