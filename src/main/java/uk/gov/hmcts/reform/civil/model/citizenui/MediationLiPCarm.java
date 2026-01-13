package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MediationLiPCarm {

    private YesOrNo isMediationContactNameCorrect;
    private String alternativeMediationContactPerson;
    private YesOrNo isMediationEmailCorrect;
    private String alternativeMediationEmail;
    private YesOrNo isMediationPhoneCorrect;
    private String alternativeMediationTelephone;
    private YesOrNo hasUnavailabilityNextThreeMonths;
    private List<Element<UnavailableDate>> unavailableDatesForMediation;
}

