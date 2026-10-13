package uk.gov.hmcts.reform.civil.controllers.fees;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.InterestCalculator.INTEREST_RATE_MUST_NOT_BE_NEGATIVE;

@ExtendWith(MockitoExtension.class)
class FeesControllerTest {

    @Mock
    private FeesService feesService;

    @Mock
    private GeneralAppFeesService generalAppFeesService;

    @Mock
    private InterestCalculator interestCalculator;

    private FeesController feesController;

    @BeforeEach
    void setUp() {
        feesController = new FeesController(feesService, generalAppFeesService, interestCalculator);
    }

    @Test
    void shouldReturnCalculatedInterest_whenValid() {
        CaseData caseData = CaseData.builder().build();
        when(interestCalculator.getInterestValidationErrors(any(CaseData.class))).thenReturn(List.of());
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(new BigDecimal("0.1"));

        ResponseEntity<BigDecimal> response = feesController.calculateClaimInterest(caseData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new BigDecimal("0.1"));
    }

    @Test
    void shouldThrowBadRequest_whenInterestRateIsNegative() {
        CaseData caseData = CaseData.builder().build();
        when(interestCalculator.getInterestValidationErrors(any(CaseData.class)))
            .thenReturn(List.of(INTEREST_RATE_MUST_NOT_BE_NEGATIVE));

        assertThatThrownBy(() -> feesController.calculateClaimInterest(caseData))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException responseStatusException = (ResponseStatusException) ex;
                assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(responseStatusException.getReason()).isEqualTo(INTEREST_RATE_MUST_NOT_BE_NEGATIVE);
            });

        verify(interestCalculator, never()).calculateInterest(any(CaseData.class));
    }
}
