package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DisclosureOfElectronicDocuments {

    @CCD(
            label = "Have you reached agreement, either using Electronic Documents Questionnaire in Practice Direction 31B or otherwise, about the scope and extent of disclosure of electronic documents on each side?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo reachedAgreement;
    @CCD(
            label = "Is such agreement likely?",
            showCondition = "reachedAgreement = \"No\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo agreementLikely;
    @CCD(
            label = "What are the issues about disclosure of electronic documents which the court needs to address, and should they be dealt with at the CMC or at a separate hearing?",
            showCondition = "agreementLikely = \"No\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String reasonForNoAgreement;
}
