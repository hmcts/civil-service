package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;

import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class PaymentDateServiceTest {

    @Mock
    private DeadlinesCalculator calculator;

    @InjectMocks
    private PaymentDateService paymentDateService;

    @Test
    void shouldGetWhenWillThisAmountBePaid_whenWhenWillThisAmountBePaidArePresent() {
        //Given
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData caseData = CaseData.builder()
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            )
            .build();
        //When
        LocalDate payDate = paymentDateService.getPaymentDateAdmittedClaim(caseData);
        //Then
        assertThat(payDate).isEqualTo(whenWillPay);
    }

    @Test
    void shouldGetWhenWasThisAmountPaid_whenWhenWasThisAmountPaidArePresent() {
        //Given
        LocalDate whenWasPaid = LocalDate.now().plusDays(-5);
        CaseData caseData = CaseData.builder()
            .respondToAdmittedClaim(RespondToClaim.builder()
                                        .whenWasThisAmountPaid(whenWasPaid).build()
            )
            .build();
        //When
        LocalDate result = paymentDateService.getPaymentDateAdmittedClaim(caseData);
        //Then
        assertThat(result).isEqualTo(whenWasPaid);
    }

    @Test
    void shouldReturnRespondent1ResponseDatePlus5Days_whenRespondent1ResponseDateArePresent() {
        //Given
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        when(calculator.calculateRespondentPaymentDateAdmittedClaim(any())).thenReturn(whenWillPay);
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .build();
        //When
        LocalDate result = paymentDateService.getPaymentDateAdmittedClaim(caseData);
        //Then
        assertThat(result).isEqualTo(whenWillPay);
    }

    @Test
    void shouldReturnNull_whenAnythingIsSettled() {
        //Given
        CaseData caseData = CaseData.builder().build();
        //When
        LocalDate result = paymentDateService.getPaymentDateAdmittedClaim(caseData);
        //Then
        assertThat(result).isNull();
    }

}
