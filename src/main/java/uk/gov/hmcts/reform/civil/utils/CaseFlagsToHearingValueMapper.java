package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.filter;

public class CaseFlagsToHearingValueMapper {

    public static boolean hasVulnerableFlag(List<FlagDetail> flagDetails) {
        return filter(flagDetails,CaseFlagPredicates.isActive(), CaseFlagPredicates.hasVulnerableFlag()).stream().count() > 1;
    }

    public static boolean getAdditionalSecurity(List<FlagDetail> flagDetails) {
        return filter(flagDetails, CaseFlagPredicates.isActive(), CaseFlagPredicates.hasAdditionalSecurityFlag()).stream().count() > 1;
    }

    public static String hasLanguageMisinterpretationFlag(List<FlagDetail> flagDetails) {
        var flags =  filter(flagDetails, CaseFlagPredicates.isActive(), CaseFlagPredicates.isHearingRelevant(),
                      CaseFlagPredicates.hasLanguageInterpreterFlag());
        return flags.stream().count() > 0 ? flags.get(0).getSubTypeValue() : null;
    }

    public static String getCustodyStatus(List<FlagDetail> flagDetails) {
        return filter(flagDetails, CaseFlagPredicates.isActive(), CaseFlagPredicates.isDetainedIndividual()).stream().count() > 1 ? "C" :  null;
    }
}
