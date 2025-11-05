package uk.gov.hmcts.reform.civil.service.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static java.lang.String.format;

@Service
public class DjNarrativeService {

    private static final String ORDER_ISSUED_HEADER = "# Your order has been issued %n%n ## Claim number %n%n # %s";
    private static final String ORDER_SENT_TEMPLATE = "The directions order has been sent to: "
        + "%n%n ## Claimant 1 %n%n %s";
    private static final String ORDER_DEFENDANT_ONE_TEMPLATE = "%n%n ## Defendant 1 %n%n %s";
    private static final String ORDER_DEFENDANT_TWO_TEMPLATE = "%n%n ## Defendant 2 %n%n %s";

    public String buildConfirmationHeader(CaseData caseData) {
        return format(ORDER_ISSUED_HEADER, caseData.getLegacyCaseReference());
    }

    public String buildConfirmationBody(CaseData caseData) {
        boolean bothDefendantsSelected = caseData.getRespondent2() != null
            && caseData.getDefendantDetails() != null
            && caseData.getDefendantDetails().getValue() != null
            && caseData.getDefendantDetails().getValue().getLabel().startsWith("Both");

        if (bothDefendantsSelected) {
            return format(ORDER_SENT_TEMPLATE, caseData.getApplicant1().getPartyName())
                + format(ORDER_DEFENDANT_ONE_TEMPLATE, caseData.getRespondent1().getPartyName())
                + format(ORDER_DEFENDANT_TWO_TEMPLATE, caseData.getRespondent2().getPartyName());
        }

        return format(ORDER_SENT_TEMPLATE, caseData.getApplicant1().getPartyName())
            + format(ORDER_DEFENDANT_ONE_TEMPLATE, caseData.getRespondent1().getPartyName());
    }
}
