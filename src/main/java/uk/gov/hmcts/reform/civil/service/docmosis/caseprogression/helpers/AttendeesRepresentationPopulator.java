package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm.JudgeFinalOrderFormBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.builders.ClaimantAttendsOrRepresentedTextBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.builders.DefendantAttendsOrRepresentedTextBuilder;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Component
public class AttendeesRepresentationPopulator {

    private final ClaimantAttendsOrRepresentedTextBuilder claimantAttendsOrRepresentedTextBuilder;
    private final DefendantAttendsOrRepresentedTextBuilder defendantAttendsOrRepresentedTextBuilder;

    public JudgeFinalOrderFormBuilder populateAttendeesDetails(JudgeFinalOrderFormBuilder builder, CaseData caseData) {
        return builder.claimantAttendsOrRepresented(generateClaimantAttendsOrRepresentedText(caseData, false))
            .claimantTwoAttendsOrRepresented(nonNull(caseData.getApplicant2())
                                                 ? generateClaimantAttendsOrRepresentedText(caseData, true) : null)
            .defendantAttendsOrRepresented(generateDefendantAttendsOrRepresentedText(caseData, false))
            .defendantTwoAttendsOrRepresented(nonNull(caseData.getRespondent2())
                                                  ? generateDefendantAttendsOrRepresentedText(caseData, true) : null)
            .otherRepresentedText(getOtherRepresentedText(caseData));
    }

    public String generateClaimantAttendsOrRepresentedText(CaseData caseData, Boolean isClaimant2) {
        return claimantAttendsOrRepresentedTextBuilder.claimantBuilder(caseData, isClaimant2);
    }

    public String generateDefendantAttendsOrRepresentedText(CaseData caseData, Boolean isDefendant2) {
        return defendantAttendsOrRepresentedTextBuilder.defendantBuilder(caseData, isDefendant2);
    }

    String getOtherRepresentedText(CaseData caseData) {
        return nonNull(caseData.getFinalOrderRepresentation())
            && nonNull(caseData.getFinalOrderRepresentation().getTypeRepresentationOtherComplex())
            ? caseData.getFinalOrderRepresentation().getTypeRepresentationOtherComplex().getDetailsRepresentationText() : "";
    }
}
