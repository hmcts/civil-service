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
            final GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();
            final CallbackParams callbackParams = new CallbackParams()
                .caseData(caseData);

            assertEquals(caseData, callbackParams.getGeneralApplicationCaseData());
        }

        @Test
        void shouldThrowIllegalStateExceptionWhenCaseDataIsNull() {
            final CallbackParams callbackParams = new CallbackParams()
                .caseData(null);
            assertThrows(IllegalStateException.class, callbackParams::getGeneralApplicationCaseData);
        }

        @Test
        void shouldThrowIllegalStateExceptionWhenCaseDataIsCaseData() {
            final CaseData caseData = CaseData.builder().build();
            final CallbackParams callbackParams = new CallbackParams()
                .caseData(caseData);
            assertThrows(IllegalStateException.class, callbackParams::getGeneralApplicationCaseData);
        }
    }

    @Nested
    class GeneralApplicationCaseDataBeforeTests {

        @Test
        void shouldReturnGeneralApplicationCaseDataWhenCaseDataBeforeIsGeneralApplicationCaseData() {
            final GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();
            final CallbackParams callbackParams = new CallbackParams()
                .caseDataBefore(caseData);

            assertEquals(caseData, callbackParams.getGeneralApplicationCaseDataBefore());
        }

        @Test
        void shouldNotThrowIllegalStateExceptionWhenCaseDataBeforeIsNull() {
            final CallbackParams callbackParams = new CallbackParams()
                .caseDataBefore(null);
            assertNull(callbackParams.getCaseDataBefore());
        }

        @Test
        void shouldThrowIllegalStateExceptionWhenCaseDataBeforeIsCaseData() {
            final CaseData caseData = CaseData.builder().build();
            final CallbackParams callbackParams = new CallbackParams()
                .caseDataBefore(caseData);
            assertThrows(IllegalStateException.class, callbackParams::getGeneralApplicationCaseDataBefore);
        }
    }

    @Nested
    class CaseDataTests {

        @Test
        void shouldReturnCaseDataWhenCaseDataIsCaseData() {
            final CaseData caseData = CaseData.builder().build();
            final CallbackParams callbackParams = new CallbackParams()
                .caseData(caseData);

            assertEquals(caseData, callbackParams.getCaseData());
        }

        @Test
        void shouldThrowIllegalStateExceptionWhenCaseDataIsNull() {
            final CallbackParams callbackParams = new CallbackParams()
                .caseData(null);
            assertThrows(IllegalStateException.class, callbackParams::getCaseData);
        }

        @Test
        void shouldThrowIllegalStateExceptionWhenCaseDataIsGeneralApplicationCaseData() {
            final GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();
            final CallbackParams callbackParams = new CallbackParams()
                .caseData(caseData);
            assertThrows(IllegalStateException.class, callbackParams::getCaseData);
        }
    }

    @Nested
    class CaseDataBeforeTests {

        @Test
        void shouldReturnCaseDataWhenCaseDataBeforeIsCaseData() {
            final CaseData caseData = CaseData.builder().build();
            final CallbackParams callbackParams = new CallbackParams()
                .caseDataBefore(caseData);

            assertEquals(caseData, callbackParams.getCaseDataBefore());
        }

        @Test
        void shouldNotThrowIllegalStateExceptionWhenCaseDataBeforeIsNull() {
            final CallbackParams callbackParams = new CallbackParams()
                .caseDataBefore(null);
            assertNull(callbackParams.getCaseDataBefore());
        }

        @Test
        void shouldThrowIllegalStateExceptionWhenCaseDataBeforeIsGeneralApplicationCaseData() {
            final GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();
            final CallbackParams callbackParams = new CallbackParams()
                .caseDataBefore(caseData);
            assertThrows(IllegalStateException.class, callbackParams::getCaseDataBefore);
        }
    }
}
