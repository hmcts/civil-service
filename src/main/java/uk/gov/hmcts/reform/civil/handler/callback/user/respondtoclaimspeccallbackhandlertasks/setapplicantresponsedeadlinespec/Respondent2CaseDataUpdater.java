package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecUtils;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferencesSpec;

@Component
@RequiredArgsConstructor
public class Respondent2CaseDataUpdater implements SetApplicantResponseDeadlineCaseDataUpdater {

    private final RespondToClaimSpecUtils respondToClaimSpecUtils;
    private final Time time;

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        LocalDateTime responseDate = time.now();
        if (respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)
                && caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
            updatedData.respondent2ClaimResponseTypeForSpec(caseData.getRespondent1ClaimResponseTypeForSpec());
            updatedData
                    .respondent2ResponseDate(responseDate);
        }

        if (ofNullable(caseData.getRespondent2()).isPresent()
                && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            Party updatedRespondent2 = caseData.getRespondent2().toBuilder()
                    .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                    .flags(caseData.getRespondent2Copy().getFlags())
                    .build();
            updatedData.respondent2(updatedRespondent2)
                    .respondent2Copy(null);
            updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());
        }

        updatedData.caseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferencesSpec(caseData));

    }
}
