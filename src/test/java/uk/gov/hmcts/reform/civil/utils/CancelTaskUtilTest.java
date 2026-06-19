package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CancelTaskUtilTest {

    @Nested
    class CancelApplicantWaDocumentUploadTask {

        @Test
        void shouldReturnTrue_whenLipvLipOneVOneAndPreferredLanguageIsEnglish() {
            CaseData data = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .changeLanguagePreference(new ChangeLanguagePreference(null, PreferredLanguage.ENGLISH))
                .build();

            assertTrue(CancelTaskUtil.cancelApplicantWaDocumentUploadTask(data));
        }

        @Test
        void shouldReturnFalse_whenChangeLanguagePreferenceIsNull() {
            CaseData data = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();

            assertFalse(CancelTaskUtil.cancelApplicantWaDocumentUploadTask(data));
        }

        @Test
        void shouldReturnFalse_whenPreferredLanguageIsWelsh() {
            CaseData data = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .changeLanguagePreference(new ChangeLanguagePreference(null, PreferredLanguage.WELSH))
                .build();

            assertFalse(CancelTaskUtil.cancelApplicantWaDocumentUploadTask(data));
        }

        @Test
        void shouldReturnFalse_whenNotLipvLip() {
            CaseData data = CaseData.builder()
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .changeLanguagePreference(new ChangeLanguagePreference(null, PreferredLanguage.ENGLISH))
                .build();

            assertFalse(CancelTaskUtil.cancelApplicantWaDocumentUploadTask(data));
        }
    }
}
