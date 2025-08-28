package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentDateService {

    private static final String DATE_PATTERN = "dd MMMM yyyy";
    private final DeadlineExtensionCalculatorService deadlineCalculatorService;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.ENGLISH);

    public String getFormattedPaymentDate(CaseData caseData) {
        return this.getPaymentDate(caseData).map(this::formatDate).orElse(null);
    }

    public Optional<LocalDate> getPaymentDate(CaseData caseData) {
        if (caseData.getRespondToClaimAdmitPartLRspec() != null
            && caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid() != null) {
            log.info("Payment date for case {} is {}", caseData.getCcdCaseReference(), caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid());
            return Optional.of(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid());
        }
        if (caseData.getRespondToAdmittedClaim() != null
            && caseData.getRespondToAdmittedClaim().getWhenWasThisAmountPaid() != null) {
            log.info("Payment date for case {} is {}", caseData.getCcdCaseReference(), caseData.getRespondToAdmittedClaim().getWhenWasThisAmountPaid());
            return Optional.of(caseData.getRespondToAdmittedClaim().getWhenWasThisAmountPaid());
        }
        if (caseData.getRespondent1ResponseDate() != null) {
            log.info("Payment date for case {} is {}", caseData.getCcdCaseReference(), RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
            return Optional.ofNullable(deadlineCalculatorService.calculateExtendedDeadline(
                caseData.getRespondent1ResponseDate().toLocalDate(),
                RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY));
        }
        return Optional.empty();
    }

    public LocalDate calculatePaymentDeadline() {
        return deadlineCalculatorService.calculateExtendedDeadline(
            ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDateTime(),
            RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
    }

    public String formatDate(LocalDate date) {
        return Optional.ofNullable(date).map(localDate -> localDate.format(DATE_FORMATTER)).orElse(null);
    }
}
