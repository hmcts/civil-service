package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefendantAmountIncludesTextParamsBuilderTest {

    private DefendantAmountIncludesTextParamsBuilder builder;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        builder = new DefendantAmountIncludesTextParamsBuilder();
        caseData = mock(CaseData.class);
    }

    @Test
    void shouldAddFullAdmissionParamsWhenPayBySetDate() {

        when(caseData.isPayBySetDate()).thenReturn(true);
        when(caseData.isPayByInstallment()).thenReturn(false);
        when(caseData.isFullAdmitClaimSpec()).thenReturn(true);
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertEquals("(this amount includes interest if it has been claimed which may continue to accrue to the date of Judgment, settlement agreement or earlier payment)",
                params.get("defendantAmountIncludesTextEn"));
        assertEquals("(mae’r swm hwn yn cynnwys llog os yw wedi’i hawlio a gall barhau i gronni hyd dyddiad y Dyfarniad, y cytundeb setlo neu daliad cynharach)",
                     params.get("defendantAmountIncludesTextCy"));
    }

    @Test
    void shouldAddFullAdmissionParamsWhenPayByInstallment() {

        when(caseData.isPayBySetDate()).thenReturn(false);
        when(caseData.isPayByInstallment()).thenReturn(true);
        when(caseData.isFullAdmitClaimSpec()).thenReturn(true);
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertEquals("(this amount includes interest if it has been claimed which may continue to accrue to the date of Judgment, settlement agreement or earlier payment)",
                params.get("defendantAmountIncludesTextEn"));
        assertEquals("(mae’r swm hwn yn cynnwys llog os yw wedi’i hawlio a gall barhau i gronni hyd dyddiad y Dyfarniad, y cytundeb setlo neu daliad cynharach)",
                     params.get("defendantAmountIncludesTextCy"));
    }

    @Test
    void shouldAddPartAdmissionParamsWhenPayBySetDate() {

        when(caseData.isPayBySetDate()).thenReturn(true);
        when(caseData.isPayByInstallment()).thenReturn(false);
        when(caseData.isPartAdmitClaimSpec()).thenReturn(true);
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertEquals("plus the claim fee and any fixed costs claimed", params.get("defendantAmountIncludesTextEn"));
        assertEquals("ynghyd â ffi’r hawliad ac unrhyw gostau sefydlog a hawlir", params.get("defendantAmountIncludesTextCy"));
    }

    @Test
    void shouldAddPartAdmissionParamsWhenPayByInstallment() {

        when(caseData.isPayBySetDate()).thenReturn(false);
        when(caseData.isPayByInstallment()).thenReturn(true);
        when(caseData.isPartAdmitClaimSpec()).thenReturn(true);
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertEquals("plus the claim fee and any fixed costs claimed", params.get("defendantAmountIncludesTextEn"));
        assertEquals("ynghyd â ffi’r hawliad ac unrhyw gostau sefydlog a hawlir", params.get("defendantAmountIncludesTextCy"));
    }

    @Test
    void shouldNotAddParamsWhenPaymentConditionsNotMet() {

        when(caseData.isPayBySetDate()).thenReturn(false);
        when(caseData.isPayByInstallment()).thenReturn(false);
        when(caseData.isFullAdmitClaimSpec()).thenReturn(true);
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertFalse(params.containsKey("defendantAmountIncludesTextEn"));
        assertFalse(params.containsKey("defendantAmountIncludesTextCy"));
    }

    @Test
    void shouldNotAddParamsWhenResponseTypeNotAdmission() {

        when(caseData.isPayBySetDate()).thenReturn(false);
        when(caseData.isPayByInstallment()).thenReturn(false);
        when(caseData.isFullAdmitClaimSpec()).thenReturn(false);
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertFalse(params.containsKey("defendantAmountIncludesTextEn"));
        assertFalse(params.containsKey("defendantAmountIncludesTextCy"));
    }
}
