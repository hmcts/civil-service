package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MediationLiPCarm {

    @CCD(label = "Is the mediation contact name correct?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isMediationContactNameCorrect;
    @CCD(label = "Alternative mediation contact name", searchable = false)
    private String alternativeMediationContactPerson;
    @CCD(label = "Is the mediation email correct?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isMediationEmailCorrect;
    @CCD(label = "Alternative mediation email", searchable = false, typeOverride = FieldType.Email)
    private String alternativeMediationEmail;
    @CCD(label = "Is the mediation phone number correct?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isMediationPhoneCorrect;
    @CCD(label = "Alternative mediation phone number", searchable = false)
    private String alternativeMediationTelephone;
    @CCD(
            label = "Are there any unavailable dates for mediation in the next three months?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo hasUnavailabilityNextThreeMonths;
    @CCD(label = "Unavailable dates for mediation", searchable = false)
    private List<Element<UnavailableDate>> unavailableDatesForMediation;
}

