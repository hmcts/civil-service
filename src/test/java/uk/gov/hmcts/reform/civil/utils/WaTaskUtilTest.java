package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaTaskUtilTest {

    @Nested
    class ConfirmIfStateChangeRequired {

        @Test
        void shouldReturnTrue_whenCancelApplicantWaDocumentUploadTaskIsTrue() {
            CaseData data = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .changeLanguagePreference(new ChangeLanguagePreference(null, PreferredLanguage.ENGLISH))
                .build();

            assertTrue(WaTaskUtil.confirmIfStateChangeRequired(data));
        }

        @ParameterizedTest
        @EnumSource(value = PreferredLanguage.class, mode = EnumSource.Mode.EXCLUDE, names = {"ENGLISH"}
        )
        void shouldReturnFalse_whenCancelApplicantWaDocumentUploadTaskIsFalse(PreferredLanguage  preferredLanguage) {
            CaseData data = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .changeLanguagePreference(new ChangeLanguagePreference(null, preferredLanguage))
                .build();

            assertFalse(WaTaskUtil.confirmIfStateChangeRequired(data));
        }
    }

    @Nested
    class CancelApplicantWaDocumentUploadTask {

        @Test
        void shouldReturnTrue_whenLipvLipOneVOneAndPreferredLanguageIsEnglish() {
            CaseData data = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .changeLanguagePreference(new ChangeLanguagePreference(null, PreferredLanguage.ENGLISH))
                .build();

            assertTrue(WaTaskUtil.cancelApplicantWaDocumentUploadTask(data));
        }

        @Test
        void shouldReturnFalse_whenChangeLanguagePreferenceIsNull() {
            CaseData data = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();

            assertFalse(WaTaskUtil.cancelApplicantWaDocumentUploadTask(data));
        }

        @Test
        void shouldReturnFalse_whenPreferredLanguageIsWelsh() {
            CaseData data = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .changeLanguagePreference(new ChangeLanguagePreference(null, PreferredLanguage.WELSH))
                .build();

            assertFalse(WaTaskUtil.cancelApplicantWaDocumentUploadTask(data));
        }

        @Test
        void shouldReturnFalse_whenNotLipvLip() {
            CaseData data = CaseData.builder()
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .changeLanguagePreference(new ChangeLanguagePreference(null, PreferredLanguage.ENGLISH))
                .build();

            assertFalse(WaTaskUtil.cancelApplicantWaDocumentUploadTask(data));
        }
    }
}
