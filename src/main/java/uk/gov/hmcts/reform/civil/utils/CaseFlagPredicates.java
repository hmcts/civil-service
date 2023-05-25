package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;

import java.util.List;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class CaseFlagPredicates {

    private CaseFlagPredicates() {
        //NO-OP
    }

    private static final List<String> VULNERABLE_FLAGS = List.of("PF0002", "RA0033", "RA0026");
    private static final List<String> ADDITIONAL_SECURITY_FLAGS = List.of("PF0007");
    private static final List<String> LANGUAGE_INTERPRETER_FLAGS = List.of("PF0015");
    private static final List<String> SIGN_LANGUAGE_INTERPRETER_FLAGS = List.of("RA0042");
    private static final List<String> DETAINED_INDIVIDUAL_FLAGS = List.of("PF0019");
    private static final List<String> REASONABLE_ADJUSTMENT_FLAGS = List.of("RA", "SM");

    public static Predicate<FlagDetail> isActive() {
        return flagDetail -> flagDetail.getStatus().equals("Active");
    }

    public static Predicate<FlagDetail> isHearingRelevant() {
        return flagDetail -> flagDetail.getHearingRelevant().equals(YES);
    }

    public static Predicate<FlagDetail> hasVulnerableFlag() {
        return flagDetail -> VULNERABLE_FLAGS.contains(flagDetail.getFlagCode());
    }

    public static Predicate<FlagDetail> hasAdditionalSecurityFlag() {
        return flagDetail -> ADDITIONAL_SECURITY_FLAGS.contains(flagDetail.getFlagCode());
    }

    public static Predicate<FlagDetail> hasLanguageInterpreterFlag() {
        return flagDetail -> LANGUAGE_INTERPRETER_FLAGS.contains(flagDetail.getFlagCode());
    }

    public static Predicate<FlagDetail> hasSignLanguageInterpreterFlag() {
        return flagDetail -> SIGN_LANGUAGE_INTERPRETER_FLAGS.contains(flagDetail.getFlagCode());
    }

    public static Predicate<FlagDetail> hasCaseInterpreterRequiredFlag() {
        return flagDetail -> SIGN_LANGUAGE_INTERPRETER_FLAGS.contains(flagDetail.getFlagCode())
            || LANGUAGE_INTERPRETER_FLAGS.contains(flagDetail.getFlagCode());
    }

    public static Predicate<FlagDetail> isDetainedIndividual() {
        return flagDetail -> DETAINED_INDIVIDUAL_FLAGS.contains(flagDetail.getFlagCode());
    }

    public static Predicate<FlagDetail> hasReasonableAdjustmentFlagCodes() {
        return flagDetail -> REASONABLE_ADJUSTMENT_FLAGS.stream().anyMatch(flagDetail.getFlagCode()::startsWith);
    }
}
