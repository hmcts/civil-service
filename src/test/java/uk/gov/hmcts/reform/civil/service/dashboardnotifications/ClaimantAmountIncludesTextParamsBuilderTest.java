package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClaimantAmountIncludesTextParamsBuilderTest {

    private ClaimantAmountIncludesTextParamsBuilder builder;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        builder = new ClaimantAmountIncludesTextParamsBuilder();
        caseData = mock(CaseData.class);
    }

    @Test
    void shouldAddFullAdmissionParams() {
        // Arrange
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_ADMISSION);
        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertEquals("(this amount includes interest if it has been claimed which may continue to accrue to the date of Judgment, " +
            "settlement agreement or earlier payment)", params.get("claimantAmountIncludesTextEn"));
        assertEquals("(mae’r swm hwn yn cynnwys llog os yw wedi’i hawlio a gall barhau i gronni hyd dyddiad y Dyfarniad, " +
            "y cytundeb setlo neu daliad cynharach)", params.get("claimantAmountIncludesTextCy"));
    }

    @Test
    void shouldAddPartAdmissionParams() {
        // Arrange
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertEquals("plus the claim fee", params.get("claimantAmountIncludesTextEn"));
        assertEquals("ynghyd â ffi’r hawliad", params.get("claimantAmountIncludesTextCy"));
    }
}
