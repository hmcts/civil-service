package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
public class Respondent1CaseDataUpdater implements SetApplicantResponseDeadlineCaseDataUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Party updatedRespondent1;
        if (NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())) {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                    .primaryAddress(caseData.getSpecAoSApplicantCorrespondenceAddressdetails())
                    .build();
        } else {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                    .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
                    .build();
        }
        updatedData.respondent1(updatedRespondent1);

        if (caseData.getRespondent1Copy() != null) {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                    .flags(caseData.getRespondent1Copy().getFlags())
                    .build();
            updatedData.respondent1(updatedRespondent1);
        }

        updatedData.respondent1Copy(null);
    }
}
