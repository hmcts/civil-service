package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTimeRouteCaseDataUpdater implements SetApplicantResponseDeadlineCaseDataUpdater {

    private final DeadlineExtensionCalculatorService deadlineCalculatorService;

    @Override
    public void update(CaseData caseData) {
        log.info("Updating PaymentTimeRouteCaseData for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.isCurrentDefendantRespondent2()) {
            updateImmediatePayByDateForRespondent2(caseData);
        } else {
            updateImmediatePayByDateForRespondent1(caseData);
        }
    }

    private void updateImmediatePayByDateForRespondent1(CaseData caseData) {
        if (IMMEDIATELY.equals(caseData.getDefenceAdmitPartPaymentTimeRouteRequired())
            && isAdmissionNeedingImmediateDeadline(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            log.debug("Respondent 1 pay immediately admission for caseId: {}", caseData.getCcdCaseReference());
            caseData.setRespondToClaimAdmitPartLRspec(buildImmediateAdmitPart());
        } else {
            log.info("Respondent 1 defence admit part payment time route is not IMMEDIATELY admission for caseId: {}",
                     caseData.getCcdCaseReference());
        }
    }

    private void updateImmediatePayByDateForRespondent2(CaseData caseData) {
        if (IMMEDIATELY.equals(caseData.getDefenceAdmitPartPaymentTimeRouteRequired2())
            && isAdmissionNeedingImmediateDeadline(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            log.debug("Respondent 2 pay immediately admission for caseId: {}", caseData.getCcdCaseReference());
            caseData.setRespondToClaimAdmitPartLRspec2(buildImmediateAdmitPart());
        } else {
            log.info("Respondent 2 defence admit part payment time route is not IMMEDIATELY admission for caseId: {}",
                     caseData.getCcdCaseReference());
        }
    }

    private RespondToClaimAdmitPartLRspec buildImmediateAdmitPart() {
        LocalDate whenBePaid = deadlineCalculatorService.calculateExtendedDeadline(
            ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDateTime(),
            RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
        return new RespondToClaimAdmitPartLRspec(whenBePaid);
    }

    private boolean isAdmissionNeedingImmediateDeadline(RespondentResponseTypeSpec responseType) {
        return FULL_ADMISSION.equals(responseType) || PART_ADMISSION.equals(responseType);
    }
}
