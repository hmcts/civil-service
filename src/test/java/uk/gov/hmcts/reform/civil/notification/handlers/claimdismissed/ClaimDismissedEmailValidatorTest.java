package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClaimDismissedEmailValidatorTest {

    private ClaimDismissedEmailValidator validator;
    private MockedStatic<MultiPartyScenario> multiPartyScenarioMock;

    @BeforeEach
    void setUp() {
        validator = new ClaimDismissedEmailValidator();
        multiPartyScenarioMock = org.mockito.Mockito.mockStatic(MultiPartyScenario.class);
    }

    @AfterEach
    void tearDown() {
        multiPartyScenarioMock.close();
    }

    @Test
    void shouldReturnTrueWhenValidForEmail() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        multiPartyScenarioMock.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);
        when(caseData.getClaimDismissedDate()).thenReturn(LocalDateTime.now());

        // Act
        boolean result = validator.isValidForEmail(caseData);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNotOneVTwoTwoLegalRep() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        multiPartyScenarioMock.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);
        when(caseData.getClaimDismissedDate()).thenReturn(LocalDateTime.now());

        // Act
        boolean result = validator.isValidForEmail(caseData);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenClaimDismissedDateIsNull() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        multiPartyScenarioMock.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);
        when(caseData.getClaimDismissedDate()).thenReturn(null);

        // Act
        boolean result = validator.isValidForEmail(caseData);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenBothConditionsAreFalse() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        multiPartyScenarioMock.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);
        when(caseData.getClaimDismissedDate()).thenReturn(null);

        // Act
        boolean result = validator.isValidForEmail(caseData);

        // Assert
        assertThat(result).isFalse();
    }
}
