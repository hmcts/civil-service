package uk.gov.hmcts.reform.civil.callback;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CallbackParamsTest {

    @Nested
    class GeneralApplicationCaseDataTests {

        @Test
        void shouldReturnGeneralApplicationCaseDataWhenCaseDataIsGeneralApplicationCaseData() {
            final GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder().build();
            final CallbackParams callbackParams = CallbackParams.builder()
                .caseData(caseData)
                .build();

            assertEquals(caseData, callbackParams.getGeneralApplicationCaseData());
        }

        @Test
        void shouldThrowIllegalStateExceptionWhenCaseDataIsNull() {
            final CallbackParams callbackParams = CallbackParams.builder()
                .caseData(null)
                .build();
            assertThrows(IllegalStateException.class, callbackParams::getGeneralApplicationCaseData);
        }

        @Test
        void shouldThrowIllegalStateExceptionWhenCaseDataIsCaseData() {
            final CaseData caseData = CaseData.builder().build();
            final CallbackParams callbackParams = CallbackParams.builder()
                .caseData(caseData)
                .build();
            assertThrows(IllegalStateException.class, callbackParams::getGeneralApplicationCaseData);
        }
    }

    @Nested
    class GeneralApplicationCaseDataBeforeTests {

        @Test
        void shouldReturnGeneralApplicationCaseDataWhenCaseDataBeforeIsGeneralApplicationCaseData() {
            final GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder().build();
            final CallbackParams callbackParams = CallbackParams.builder()
                .caseDataBefore(caseData)
                .build();

            assertEquals(caseData, callbackParams.getGeneralApplicationCaseDataBefore());
        }

        @Test
        void shouldNotThrowIllegalStateExceptionWhenCaseDataBeforeIsNull() {
            final CallbackParams callbackParams = CallbackParams.builder()
                .caseDataBefore(null)
                .build();
            assertNull(callbackParams.getCaseDataBefore());
        }

        @Test
        void shouldThrowIllegalStateExceptionWhenCaseDataBeforeIsCaseData() {
            final CaseData caseData = CaseData.builder().build();
            final CallbackParams callbackParams = CallbackParams.builder()
                .caseDataBefore(caseData)
                .build();
            assertThrows(IllegalStateException.class, callbackParams::getGeneralApplicationCaseDataBefore);
        }
    }

    @Nested
    class CaseDataTests {

        @Test
        void shouldReturnCaseDataWhenCaseDataIsCaseData() {
            final CaseData caseData = CaseData.builder().build();
            final CallbackParams callbackParams = CallbackParams.builder()
                .caseData(caseData)
                .build();

            assertEquals(caseData, callbackParams.getCaseData());
        }

        @Test
        void shouldThrowIllegalStateExceptionWhenCaseDataIsNull() {
            final CallbackParams callbackParams = CallbackParams.builder()
                .caseData(null)
                .build();
            assertThrows(IllegalStateException.class, callbackParams::getCaseData);
        }

        @Test
        void shouldThrowIllegalStateExceptionWhenCaseDataIsGeneralApplicationCaseData() {
            final GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder().build();
            final CallbackParams callbackParams = CallbackParams.builder()
                .caseData(caseData)
                .build();
            assertThrows(IllegalStateException.class, callbackParams::getCaseData);
        }
    }

    @Nested
    class CaseDataBeforeTests {

        @Test
        void shouldReturnCaseDataWhenCaseDataBeforeIsCaseData() {
            final CaseData caseData = CaseData.builder().build();
            final CallbackParams callbackParams = CallbackParams.builder()
                .caseDataBefore(caseData)
                .build();

            assertEquals(caseData, callbackParams.getCaseDataBefore());
        }

        @Test
        void shouldNotThrowIllegalStateExceptionWhenCaseDataBeforeIsNull() {
            final CallbackParams callbackParams = CallbackParams.builder()
                .caseDataBefore(null)
                .build();
            assertNull(callbackParams.getCaseDataBefore());
        }

        @Test
        void shouldThrowIllegalStateExceptionWhenCaseDataBeforeIsGeneralApplicationCaseData() {
            final GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder().build();
            final CallbackParams callbackParams = CallbackParams.builder()
                .caseDataBefore(caseData)
                .build();
            assertThrows(IllegalStateException.class, callbackParams::getCaseDataBefore);
        }
    }
}
