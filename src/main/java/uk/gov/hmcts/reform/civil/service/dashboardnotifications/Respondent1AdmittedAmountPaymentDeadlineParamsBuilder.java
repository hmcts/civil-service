package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;

import static java.util.Objects.nonNull;

@Component
public class Respondent1AdmittedAmountPaymentDeadlineParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (caseData.isPartAdmitPayImmediatelyClaimSpec()) {
            var defendantAdmittedAmount = params.get("defendantAdmittedAmount") != null ? params.get("defendantAdmittedAmount") : "";
            LocalDate whenWillThisAmountBePaid = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
            var amountIncludesTextEn = DateUtils.formatDate(whenWillThisAmountBePaid);
            var amountIncludesTextCy = DateUtils.formatDateInWelsh(whenWillThisAmountBePaid, false);

            var descriptionEn = String.format("<p class=\"govuk-body\">You've said you owe £%s plus the claim fee and " +
                                                  "any fixed costs claimed and offered to pay %s immediately. " +
                                                  "We will contact you when the claimant responds." +
                                                  "</p>",
                                              defendantAdmittedAmount,
                                              amountIncludesTextEn);

            var descriptionCy = String.format("<p class=\"govuk-body\">Rydych chi wedi dweud bod £%s yn ddyledus gennych, " +
                                                  "a ffi’r hawliad ac unrhyw gostau sefydlog a hawlir ac rydych wedi cynnig i dalu %s ar unwaith. " +
                                                  "Byddwn yn cysylltu â chi pan fydd yr hawlydd yn ymateb.</p>",
                                              defendantAdmittedAmount,
                                              amountIncludesTextCy);
            params.put(
                "descriptionEn",
                descriptionEn
            );
            params.put(
                "descriptionCy",
                descriptionCy
            );
        } else if (nonNull(caseData.getRespondToClaimAdmitPartLRspec())) {
            var defendantAdmittedAmount = params.get("defendantAdmittedAmount") != null ? params.get("defendantAdmittedAmount") : "";
            LocalDate whenWillThisAmountBePaid = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
            var respondent1AdmittedAmountPaymentDeadlineEn = DateUtils.formatDate(whenWillThisAmountBePaid);
            var respondent1AdmittedAmountPaymentDeadlineCy = DateUtils.formatDateInWelsh(whenWillThisAmountBePaid, false);
            var amountIncludesTextEn = params.get("amountIncludesTextEn") != null ? params.get("amountIncludesTextEn").toString() : "";
            var amountIncludesTextCy = params.get("amountIncludesTextCy") != null ? params.get("amountIncludesTextCy").toString() : "";
            var applicant1PartyName = params.get("applicant1PartyName") != null ? params.get("applicant1PartyName").toString() : "";
            var descriptionEn = String.format("<p class=\"govuk-body\">You have offered to pay %s by %s%s. The payment must be received in %s''s account by then, " +
                                                  "if not they can request a county court judgment.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">View your response</a></p>",
                                              defendantAdmittedAmount,
                                              respondent1AdmittedAmountPaymentDeadlineEn,
                                              amountIncludesTextEn,
                                              applicant1PartyName);

            var descriptionCy = String.format("<p class=\"govuk-body\">Rydych wedi cynnig talu %s erbyn %s%s. Rhaid i’r taliad fod yng nghyfrif %s erbyn y dyddiad hwnnw. Os nad yw, yna gallant wneud cais am ddyfarniad llys sirol.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">Gweld eich ymateb</a></p>",
                                              defendantAdmittedAmount,
                                              respondent1AdmittedAmountPaymentDeadlineCy,
                                              amountIncludesTextCy,
                                              applicant1PartyName);

            params.put(
                "descriptionEn",
                descriptionEn
            );
            params.put(
                "descriptionCy",
                descriptionCy
            );
        }

        if (nonNull(caseData.getRespondToClaimAdmitPartLRspec())) {
            LocalDate whenWillThisAmountBePaid = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
            params.put("respondent1AdmittedAmountPaymentDeadline", whenWillThisAmountBePaid.atTime(END_OF_DAY));
            params.put("respondent1AdmittedAmountPaymentDeadlineEn", DateUtils.formatDate(whenWillThisAmountBePaid));
            params.put(
                "respondent1AdmittedAmountPaymentDeadlineCy",
                DateUtils.formatDateInWelsh(whenWillThisAmountBePaid, false)
            );
        }
    }
}
