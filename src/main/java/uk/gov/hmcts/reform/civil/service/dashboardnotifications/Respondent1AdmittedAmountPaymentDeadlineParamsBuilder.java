package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Component
public class Respondent1AdmittedAmountPaymentDeadlineParamsBuilder extends DashboardNotificationsParamsBuilder {

    private static final String PARAM_AMOUNT_INCLUDES_TEXT_EN = "amountIncludesTextEn";
    private static final String PARAM_AMOUNT_INCLUDES_TEXT_CY = "amountIncludesTextCy";
    private static final String PARAM_APPLICANT1_PARTY_NAME = "applicant1PartyName";
    private static final String PARAM_DEFENDANT_ADMITTED_AMOUNT = "defendantAdmittedAmount";
    private static final String PARAM_DESCRIPTION_EN = "descriptionEn";
    private static final String PARAM_DESCRIPTION_CY = "descriptionCy";
    private static final String RESP1_ADMITTED_AMOUNT_DEADLINE = "respondent1AdmittedAmountPaymentDeadline";
    private static final String RESP1_ADMITTED_AMOUNT_DEADLINE_EN = "respondent1AdmittedAmountPaymentDeadlineEn";
    private static final String RESP1_ADMITTED_AMOUNT_DEADLINE_CY = "respondent1AdmittedAmountPaymentDeadlineCy";
    private static final boolean PAD_DAYS = false;

    private static String getStringParam(Map<String, Object> params, String key) {
        return java.util.Objects.toString(params.get(key), "");
    }

    private static LocalDate getPaymentDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec())
            .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid)
            .orElse(null);
    }

    private static String formatDateEn(LocalDate date) {
        return DateUtils.formatDate(date);
    }

    private static String formatDateCy(LocalDate date) {
        return DateUtils.formatDateInWelsh(date, PAD_DAYS);
    }

    private static void putDescriptions(Map<String, Object> params, String descriptionEn, String descriptionCy) {
        params.put(PARAM_DESCRIPTION_EN, descriptionEn);
        params.put(PARAM_DESCRIPTION_CY, descriptionCy);
    }

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        final String defendantAdmittedAmount = getStringParam(params, PARAM_DEFENDANT_ADMITTED_AMOUNT);
        final LocalDate paymentDate = getPaymentDate(caseData);

        if (paymentDate == null) {
            return; // no date → nothing to build safely
        }

        final String paymentDateEn = formatDateEn(paymentDate);
        final String paymentDateCy = formatDateCy(paymentDate);
        final String applicant1PartyName = getStringParam(params, PARAM_APPLICANT1_PARTY_NAME);

        if (caseData.isPartAdmitPayImmediatelyClaimSpec()) {
            String descriptionEn = String.format("<p class=\"govuk-body\">You've said you owe %s plus the claim fee and " +
                                                  "any fixed costs claimed and offered to pay %s immediately. " +
                                                  "We will contact you when the claimant responds." +
                                                  "</p>",
                                              defendantAdmittedAmount,
                                                 applicant1PartyName);

            String descriptionCy = String.format("<p class=\"govuk-body\">Rydych chi wedi dweud bod %s yn ddyledus gennych, " +
                                                  "a ffi’r hawliad ac unrhyw gostau sefydlog a hawlir ac rydych wedi cynnig i dalu %s ar unwaith. " +
                                                  "Byddwn yn cysylltu â chi pan fydd yr hawlydd yn ymateb.</p>",
                                              defendantAdmittedAmount,
                                                 applicant1PartyName);
            putDescriptions(params, descriptionEn, descriptionCy);

        } else if (nonNull(caseData.getRespondToClaimAdmitPartLRspec())) {
            final String amountIncludesTextEn = getStringParam(params, PARAM_AMOUNT_INCLUDES_TEXT_EN);
            final String amountIncludesTextCy = getStringParam(params, PARAM_AMOUNT_INCLUDES_TEXT_CY);

            var descriptionEn = String.format("<p class=\"govuk-body\">You have offered to pay %s by %s%s. The payment must be received in %s's account by then, " +
                                                  "if not they can request a county court judgment.</p><p class=\"govuk-body\">" +
                                                  "<a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">View your response</a></p>",
                                              defendantAdmittedAmount,
                                              paymentDateEn,
                                              amountIncludesTextEn,
                                              applicant1PartyName);

            var descriptionCy = String.format("<p class=\"govuk-body\">Rydych wedi cynnig talu %s erbyn %s%s. Rhaid i’r taliad " +
                                                  "fod yng nghyfrif %s erbyn y dyddiad hwnnw. Os nad yw, " +
                                                  "yna gallant wneud cais am ddyfarniad llys sirol.</p><p class=\"govuk-body\">" +
                                                  "<a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">Gweld eich ymateb</a></p>",
                                              defendantAdmittedAmount,
                                              paymentDateCy,
                                              amountIncludesTextCy,
                                              applicant1PartyName);

            putDescriptions(params, descriptionEn, descriptionCy);
        }

        if (nonNull(caseData.getRespondToClaimAdmitPartLRspec())) {
            putPaymentDeadlineParams(params, paymentDate, paymentDateEn, paymentDateCy);

        }
    }

    private static void putPaymentDeadlineParams(Map<String, Object> params,
                                                 LocalDate paymentDate,
                                                 String paymentDateEn,
                                                 String paymentDateCy) {
        params.put(RESP1_ADMITTED_AMOUNT_DEADLINE, paymentDate.atTime(END_OF_DAY));
        params.put(RESP1_ADMITTED_AMOUNT_DEADLINE_EN, paymentDateEn);
        params.put(RESP1_ADMITTED_AMOUNT_DEADLINE_CY, paymentDateCy);
    }

}
