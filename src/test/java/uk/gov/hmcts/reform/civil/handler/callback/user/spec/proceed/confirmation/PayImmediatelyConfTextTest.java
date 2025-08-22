package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.PaymentDateService;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PayImmediatelyConfTextTest {

    @Mock
    private PaymentDateService paymentDateService;

    @Mock
    private ClaimUrlsConfiguration claimUrlsConfiguration;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private PayImmediatelyConfText generator;

    @Test
    void shouldReturnEmpty_whenNotFullOrPartAdmitPayImmediately() {
        CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .build();

        Optional<String> out = generator.generateTextFor(caseData, featureToggleService);

        assertTrue(out.isEmpty());
        verifyNoInteractions(paymentDateService);
    }

    @Test
    void shouldThrow_whenPaymentDateServiceReturnsNull() {
        CaseData caseData = CaseData.builder()
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .applicant1ProceedWithClaim(null)
                .build();

        when(paymentDateService.getPaymentDateAdmittedClaim(caseData)).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> generator.generateTextFor(caseData, featureToggleService));
        verify(paymentDateService, times(1)).getPaymentDateAdmittedClaim(caseData);
    }

    @Test
    void shouldGenerateText_forFullAdmitImmediately_elseBranchOfMessage() {
        CaseData caseData = CaseData.builder()
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .applicant1ProceedWithClaim(null)
                .build();

        LocalDate payBy = LocalDate.of(2025, 1, 2);
        when(paymentDateService.getPaymentDateAdmittedClaim(caseData)).thenReturn(payBy);

        Optional<String> out = generator.generateTextFor(caseData, featureToggleService);

        assertTrue(out.isPresent());
        assertTrue(out.get().contains("2025"));
        verify(paymentDateService, times(1)).getPaymentDateAdmittedClaim(caseData);
    }
}
