package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CertofScParamsBuilderTest {

    private CertofScParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new CertofScParamsBuilder();
    }

    @Test
    void shouldAddParamsWhenCertOfSCIsPresent() {
        // Arrange
        LocalDate finalPaymentDate = LocalDate.of(2023, 12, 31);
        CertOfSC certOfSC = mock(CertOfSC.class);
        when(certOfSC.getDefendantFinalPaymentDate()).thenReturn(finalPaymentDate);

        CaseData caseData = mock(CaseData.class);
        when(caseData.getCertOfSC()).thenReturn(certOfSC);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("coscFullPaymentDateEn", DateUtils.formatDate(finalPaymentDate));
        assertThat(params).containsEntry("coscFullPaymentDateCy", DateUtils.formatDateInWelsh(finalPaymentDate, false));
        assertThat(params).containsEntry("coscNotificationDateEn", DateUtils.formatDate(LocalDate.now()));
        assertThat(params).containsEntry("coscNotificationDateCy", DateUtils.formatDateInWelsh(LocalDate.now(), false));
    }

    @Test
    void shouldNotAddParamsWhenCertOfSCIsNull() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCertOfSC()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
