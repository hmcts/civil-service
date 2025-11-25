package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanguagePredicateTest {

    @Mock
    private CaseData caseData;

    @Test
    void should_return_true_for_responseIsBilingual_when_case_data_is_bilingual() {
        when(caseData.isRespondentResponseBilingual()).thenReturn(true);
        assertTrue(LanguagePredicate.respondentIsBilingual.test(caseData));
    }

    @Test
    void should_return_false_for_responseIsBilingual_when_case_data_is_not_bilingual() {
        when(caseData.isRespondentResponseBilingual()).thenReturn(false);
        assertFalse(LanguagePredicate.respondentIsBilingual.test(caseData));
    }

    @Test
    void should_return_true_for_onlyInitialResponseIsBilingual_when_only_initial_response_is_bilingual_and_no_language_change() {
        when(caseData.isRespondentResponseBilingual()).thenReturn(true);
        when(caseData.getChangeLanguagePreference()).thenReturn(null);
        assertTrue(LanguagePredicate.onlyInitialResponseIsBilingual.test(caseData));
    }

    @Test
    void should_return_false_for_onlyInitialResponseIsBilingual_when_response_is_not_bilingual() {
        when(caseData.isRespondentResponseBilingual()).thenReturn(false);
        assertFalse(LanguagePredicate.onlyInitialResponseIsBilingual.test(caseData));
    }

    @Test
    void should_return_false_for_onlyInitialResponseIsBilingual_when_response_is_bilingual_and_language_has_changed() {
        when(caseData.isRespondentResponseBilingual()).thenReturn(true);
        when(caseData.getChangeLanguagePreference()).thenReturn(ChangeLanguagePreference.builder().build());
        assertFalse(LanguagePredicate.onlyInitialResponseIsBilingual.test(caseData));
    }

}
