package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class PaymentDateServiceTest {

    @Mock
    private DeadlineExtensionCalculatorService deadlineCalculatorService;

    @InjectMocks
    private PaymentDateService paymentDateService;

    @Test
    void shouldGetWhenWillThisAmountBePaid_whenWhenWillThisAmountBePaidArePresent() {
        //Given
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenWillPay);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
        //When
        Optional<LocalDate> payDate = paymentDateService.getPaymentDate(caseData);
        //Then
        assertThat(payDate.orElse(null)).isEqualTo(whenWillPay);
    }

    @Test
    void shouldGetWhenWasThisAmountPaid_whenWhenWasThisAmountPaidArePresent() {
        //Given
        LocalDate whenWasPaid = LocalDate.now().plusDays(-5);
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setWhenWasThisAmountPaid(whenWasPaid);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondToAdmittedClaim(respondToClaim);
        //When
        Optional<LocalDate> result = paymentDateService.getPaymentDate(caseData);
        //Then
        assertThat(result.orElse(null)).isEqualTo(whenWasPaid);
    }

    @Test
    void shouldReturnRespondent1ResponseDatePlus5Days_whenRespondent1ResponseDateArePresent() {
        //Given
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        when(deadlineCalculatorService.calculateExtendedDeadline(any(LocalDate.class), anyInt())).thenReturn(whenWillPay);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1ResponseDate(LocalDateTime.now());
        //When
        Optional<LocalDate> result = paymentDateService.getPaymentDate(caseData);
        //Then
        assertThat(result.orElse(null)).isEqualTo(whenWillPay);
    }

    @Test
    void shouldReturnNull_whenAnythingIsSettled() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().build();
        //When
        Optional<LocalDate> result = paymentDateService.getPaymentDate(caseData);
        //Then
        assertThat(result.orElse(null)).isNull();
    }

}
