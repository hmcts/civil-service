package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.MarkPaidConsentList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SettleClaimPaidInFullDateParamsBuilderTest {

    private SettleClaimPaidInFullDateParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new SettleClaimPaidInFullDateParamsBuilder();
    }

    @Test
    void shouldAddPaidInFullDateParamsWhenMarkPaidConsentIsYes() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(caseData.getMarkPaidConsent()).thenReturn(MarkPaidConsentList.YES);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("settleClaimPaidInFullDateEn", DateUtils.formatDate(LocalDate.now()));
        assertThat(params).containsEntry("settleClaimPaidInFullDateCy", DateUtils.formatDateInWelsh(LocalDate.now(), false));
    }

    @Test
    void shouldNotAddPaidInFullDateParamsWhenMarkPaidConsentIsNull() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(caseData.getMarkPaidConsent()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }

    @Test
    void shouldNotAddPaidInFullDateParamsWhenMarkPaidConsentIsNotYes() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(caseData.getMarkPaidConsent()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
