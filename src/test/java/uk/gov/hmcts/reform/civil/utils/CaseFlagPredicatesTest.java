package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class CaseFlagPredicatesTest {

    @Test
    void isActive_shouldReturnExpectedActiveFlagDetails() {
        FlagDetail inactiveFlagDetail = FlagDetail.builder().status("INACTIVE").build();
        FlagDetail activeFlagDetail = FlagDetail.builder().status("Active").build();

        assertFalse(CaseFlagPredicates.isActive().test(inactiveFlagDetail));
        assertTrue(CaseFlagPredicates.isActive().test(activeFlagDetail));
    }

    @Test
    void isHearingRelevant_shouldReturnExpectedHearingRelevantFlagDetails() {
        FlagDetail nonHearingRelevantFlagDetail = FlagDetail.builder().hearingRelevant(NO).build();
        FlagDetail hearingRelevantFlagDetail = FlagDetail.builder().hearingRelevant(YES).build();

        assertFalse(CaseFlagPredicates.isHearingRelevant().test(nonHearingRelevantFlagDetail));
        assertTrue(CaseFlagPredicates.isHearingRelevant().test(hearingRelevantFlagDetail));
    }

    @Test
    void hasVulnerableFlag_shouldReturnExpectedVulnerableFlagDetails() {
        FlagDetail nonVulnerableFlagDetail = FlagDetail.builder().flagCode("PF0007").build();
        FlagDetail vulnerableFlagDetail = FlagDetail.builder().flagCode("PF0002").build();

        assertFalse(CaseFlagPredicates.hasVulnerableFlag().test(nonVulnerableFlagDetail));
        assertTrue(CaseFlagPredicates.hasVulnerableFlag().test(vulnerableFlagDetail));
    }

    @Test
    void hasAdditionalSecurityFlag_shouldReturnExpectedAdditionalSecurityFlagDetails() {
        FlagDetail nonAdditionalSecurityFlagDetail = FlagDetail.builder().flagCode("PF0002").build();
        FlagDetail additionalSecurityFlagDetail = FlagDetail.builder().flagCode("PF0007").build();

        assertFalse(CaseFlagPredicates.hasAdditionalSecurityFlag().test(nonAdditionalSecurityFlagDetail));
        assertTrue(CaseFlagPredicates.hasAdditionalSecurityFlag().test(additionalSecurityFlagDetail));
    }

    @Test
    void hasLanguageInterpreterFlag_shouldReturnExpectedLanguageInterpreterFlagDetails() {
        FlagDetail nonLanguageInterpreterFlagDetail = FlagDetail.builder().flagCode("PF0002").build();
        FlagDetail languageInterpreterFlagDetail = FlagDetail.builder().flagCode("PF0015").build();

        assertFalse(CaseFlagPredicates.hasLanguageInterpreterFlag().test(nonLanguageInterpreterFlagDetail));
        assertTrue(CaseFlagPredicates.hasLanguageInterpreterFlag().test(languageInterpreterFlagDetail));
    }

    @Test
    void hasCaseInterpreterRequiredFlag_shouldReturnExpectedCaseInterpreterFlagDetails() {
        FlagDetail nonLanguageInterpreterFlagDetail = FlagDetail.builder().flagCode("PF0002").build();
        FlagDetail languageInterpreterFlagDetail = FlagDetail.builder().flagCode("PF0015").build();
        FlagDetail signLanguageInterpreterFlagDetail = FlagDetail.builder().flagCode("RA0042").build();

        assertFalse(CaseFlagPredicates.hasCaseInterpreterRequiredFlag().test(nonLanguageInterpreterFlagDetail));
        assertTrue(CaseFlagPredicates.hasCaseInterpreterRequiredFlag().test(languageInterpreterFlagDetail));
        assertTrue(CaseFlagPredicates.hasCaseInterpreterRequiredFlag().test(signLanguageInterpreterFlagDetail));
    }

    @Test
    void isDetainedIndividual_shouldReturnExpectedDetainedIndividualFlagDetails() {
        FlagDetail nonDetainedIndividualFlagDetail = FlagDetail.builder().flagCode("PF0002").build();
        FlagDetail detainedIndividualFlagDetail = FlagDetail.builder().flagCode("PF0019").build();

        assertFalse(CaseFlagPredicates.isDetainedIndividual().test(nonDetainedIndividualFlagDetail));
        assertTrue(CaseFlagPredicates.isDetainedIndividual().test(detainedIndividualFlagDetail));
    }

    @Test
    void hasReasonableAdjustmentFlagCodes_shouldReturnExpectedReasonableAdjustmentFlagCodes() {
        FlagDetail nonReasonableAdjustmentFlagCode = FlagDetail.builder().flagCode("PF0019").build();
        FlagDetail reasonableAdjustmentFlagCode = FlagDetail.builder().flagCode("RA0033").build();

        assertFalse(CaseFlagPredicates.hasReasonableAdjustmentFlagCodes().test(nonReasonableAdjustmentFlagCode));
        assertTrue(CaseFlagPredicates.hasReasonableAdjustmentFlagCodes().test(reasonableAdjustmentFlagCode));
    }
}
