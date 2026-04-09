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
        FlagDetail inactiveFlagDetail = new FlagDetail().setStatus("INACTIVE");
        FlagDetail activeFlagDetail = new FlagDetail().setStatus("Active");

        assertFalse(CaseFlagPredicates.isActive().test(inactiveFlagDetail));
        assertTrue(CaseFlagPredicates.isActive().test(activeFlagDetail));
    }

    @Test
    void isHearingRelevant_shouldReturnExpectedHearingRelevantFlagDetails() {
        FlagDetail nonHearingRelevantFlagDetail = new FlagDetail().setHearingRelevant(NO);
        FlagDetail hearingRelevantFlagDetail = new FlagDetail().setHearingRelevant(YES);

        assertFalse(CaseFlagPredicates.isHearingRelevant().test(nonHearingRelevantFlagDetail));
        assertTrue(CaseFlagPredicates.isHearingRelevant().test(hearingRelevantFlagDetail));
    }

    @Test
    void hasVulnerableFlag_shouldReturnExpectedVulnerableFlagDetails() {
        FlagDetail nonVulnerableFlagDetail = new FlagDetail().setFlagCode("PF0007");
        FlagDetail vulnerableFlagDetail = new FlagDetail().setFlagCode("PF0002");

        assertFalse(CaseFlagPredicates.hasVulnerableFlag().test(nonVulnerableFlagDetail));
        assertTrue(CaseFlagPredicates.hasVulnerableFlag().test(vulnerableFlagDetail));
    }

    @Test
    void hasAdditionalSecurityFlag_shouldReturnExpectedAdditionalSecurityFlagDetails() {
        FlagDetail nonAdditionalSecurityFlagDetail = new FlagDetail().setFlagCode("PF0002");
        FlagDetail additionalSecurityFlagDetail = new FlagDetail().setFlagCode("PF0007");

        assertFalse(CaseFlagPredicates.hasAdditionalSecurityFlag().test(nonAdditionalSecurityFlagDetail));
        assertTrue(CaseFlagPredicates.hasAdditionalSecurityFlag().test(additionalSecurityFlagDetail));
    }

    @Test
    void hasLanguageInterpreterFlag_shouldReturnExpectedLanguageInterpreterFlagDetails() {
        FlagDetail nonLanguageInterpreterFlagDetail = new FlagDetail().setFlagCode("PF0002");
        FlagDetail languageInterpreterFlagDetail = new FlagDetail().setFlagCode("PF0015");

        assertFalse(CaseFlagPredicates.hasLanguageInterpreterFlag().test(nonLanguageInterpreterFlagDetail));
        assertTrue(CaseFlagPredicates.hasLanguageInterpreterFlag().test(languageInterpreterFlagDetail));
    }

    @Test
    void hasCaseInterpreterRequiredFlag_shouldReturnExpectedCaseInterpreterFlagDetails() {
        FlagDetail nonLanguageInterpreterFlagDetail = new FlagDetail().setFlagCode("PF0002");
        FlagDetail languageInterpreterFlagDetail = new FlagDetail().setFlagCode("PF0015");
        FlagDetail signLanguageInterpreterFlagDetail = new FlagDetail().setFlagCode("RA0042");

        assertFalse(CaseFlagPredicates.hasCaseInterpreterRequiredFlag().test(nonLanguageInterpreterFlagDetail));
        assertTrue(CaseFlagPredicates.hasCaseInterpreterRequiredFlag().test(languageInterpreterFlagDetail));
        assertTrue(CaseFlagPredicates.hasCaseInterpreterRequiredFlag().test(signLanguageInterpreterFlagDetail));
    }

    @Test
    void isDetainedIndividual_shouldReturnExpectedDetainedIndividualFlagDetails() {
        FlagDetail nonDetainedIndividualFlagDetail = new FlagDetail().setFlagCode("PF0002");
        FlagDetail detainedIndividualFlagDetail = new FlagDetail().setFlagCode("PF0019");

        assertFalse(CaseFlagPredicates.isDetainedIndividual().test(nonDetainedIndividualFlagDetail));
        assertTrue(CaseFlagPredicates.isDetainedIndividual().test(detainedIndividualFlagDetail));
    }

    @Test
    void hasReasonableAdjustmentFlagCodes_shouldReturnExpectedReasonableAdjustmentFlagCodes() {
        FlagDetail nonReasonableAdjustmentFlagCode = new FlagDetail().setFlagCode("PF0019");
        FlagDetail reasonableAdjustmentFlagCode = new FlagDetail().setFlagCode("RA0033");

        assertFalse(CaseFlagPredicates.hasReasonableAdjustmentFlagCodes().test(nonReasonableAdjustmentFlagCode));
        assertTrue(CaseFlagPredicates.hasReasonableAdjustmentFlagCodes().test(reasonableAdjustmentFlagCode));
    }
}
