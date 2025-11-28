package uk.gov.hmcts.reform.civil.callback;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CallbackParamsTest {

    @Test
    void shouldReturnGeneralApplicationCaseDataWhenBaseCaseDataIsGeneralApplicationCaseData() {
        final GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder().build();
        final CallbackParams callbackParams = CallbackParams.builder()
            .baseCaseData(caseData)
            .build();

        assertEquals(caseData, callbackParams.getGeneralApplicationCaseData());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenBaseCaseDataIsNull() {
        final CallbackParams callbackParams = CallbackParams.builder()
            .baseCaseData(null)
            .build();
        assertThrows(IllegalStateException.class, callbackParams::getGeneralApplicationCaseData);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenBaseCaseDataIsCaseData() {
        final CaseData caseData = CaseData.builder().build();
        final CallbackParams callbackParams = CallbackParams.builder()
            .baseCaseData(caseData)
            .build();
        assertThrows(IllegalStateException.class, callbackParams::getGeneralApplicationCaseData);
    }
}
