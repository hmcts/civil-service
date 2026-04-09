package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTimeRouteCaseDataUpdater implements SetApplicantResponseDeadlineCaseDataUpdater {

    private final DeadlineExtensionCalculatorService deadlineCalculatorService;

    @Override
    public void update(CaseData caseData) {
        log.info("Updating PaymentTimeRouteCaseData for caseId: {}", caseData.getCcdCaseReference());

        if (IMMEDIATELY.equals(caseData.getDefenceAdmitPartPaymentTimeRouteRequired())
                && isFullAdmission(caseData)) {
            log.debug("Defence admit part payment time route is IMMEDIATELY and response type is full admission for caseId: {}", caseData.getCcdCaseReference());
            LocalDate whenBePaid = deadlineCalculatorService.calculateExtendedDeadline(
                    ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDateTime(),
                    RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
            RespondToClaimAdmitPartLRspec admitPartLRspec = new RespondToClaimAdmitPartLRspec();
            admitPartLRspec.setWhenWillThisAmountBePaid(whenBePaid);
            caseData.setRespondToClaimAdmitPartLRspec(admitPartLRspec);
        } else {
            log.info("Defence admit part payment time route is not IMMEDIATELY for caseId: {}", caseData.getCcdCaseReference());
        }
    }

    private boolean isFullAdmission(CaseData caseData) {
        log.debug("Checking if response type is full admission for caseId: {}", caseData.getCcdCaseReference());
        return FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || FULL_ADMISSION.equals(
            caseData.getRespondent2ClaimResponseTypeForSpec());
    }
}
