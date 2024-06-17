package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.utils.CaseFlagPredicates;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagPredicates.LANGUAGE_INTERPRETER_FLAG;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagPredicates.SIGN_LANGUAGE_INTERPRETER_FLAG;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.filter;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.getAllCaseFlags;

public class CaseFlagsToHearingValueMapper {

    private CaseFlagsToHearingValueMapper() {
        //NO-OP
    }

    public static boolean hasVulnerableFlag(List<FlagDetail> flagDetails) {
        return filter(
            flagDetails,
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.hasVulnerableFlag()
        ).stream().count() > 0;
    }

    public static String getVulnerabilityDetails(List<FlagDetail> flagDetails) {
        List<String> details = filter(
            flagDetails,
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.hasVulnerableFlag()
        ).stream()
            .map(flagDetail -> String.format(flagDetail.getFlagComment() != null ? "%s - %s" : "%s", flagDetail.getName(), flagDetail.getFlagComment()))
            .toList();

        return details.isEmpty() ? null : details.stream().map(Objects::toString).collect(Collectors.joining("; "));
    }

    public static boolean getAdditionalSecurity(List<FlagDetail> flagDetails) {
        return filter(
            flagDetails,
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.hasAdditionalSecurityFlag()
        ).stream().count() > 0;
    }

    public static String getInterpreterLanguage(List<FlagDetail> flagDetails) {
        String spokenLanguageInterpreter = getSpokenLanguageInterpreter(flagDetails);
        String signLanguageInterpreter = getSignLanguageInterpreter(flagDetails);

        return spokenLanguageInterpreter != null ? spokenLanguageInterpreter : signLanguageInterpreter;
    }

    private static List<FlagDetail> getSpokenLanguageInterpreterFlags(List<FlagDetail> flagDetails) {
        return filter(
            flagDetails,
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.isHearingRelevant(),
            CaseFlagPredicates.hasLanguageInterpreterFlag()
        );
    }

    private static String getSpokenLanguageInterpreter(List<FlagDetail> flagDetails) {
        var flags = getSpokenLanguageInterpreterFlags(flagDetails);

        return flags.stream().count() > 0 ? flags.get(0).getSubTypeKey() : null;
    }

    private static String getSignLanguageInterpreter(List<FlagDetail> flagDetails) {
        var flags = filter(
            flagDetails,
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.isHearingRelevant(),
            CaseFlagPredicates.hasSignLanguageInterpreterFlag()
        );

        return flags.stream().count() > 0 ? flags.get(0).getSubTypeKey() : null;
    }

    public static String getCustodyStatus(List<FlagDetail> flagDetails) {
        return filter(
            flagDetails,
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.isDetainedIndividual()
        ).stream().count() > 0 ? "C" : null;
    }

    public static boolean hasCaseInterpreterRequiredFlag(CaseData caseData) {
        return filter(
            getAllCaseFlags(caseData),
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.isHearingRelevant(),
            CaseFlagPredicates.hasCaseInterpreterRequiredFlag()
        ).stream().count() > 0;
    }

    private static List<FlagDetail> getReasonableAdjustmentFlags(List<FlagDetail> flagDetails) {
        return filter(
            flagDetails,
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.isHearingRelevant(),
            CaseFlagPredicates.hasReasonableAdjustmentFlagCodes()
        );
    }

    public static List<String> getReasonableAdjustments(List<FlagDetail> flagDetails) {
        return getReasonableAdjustmentFlags(flagDetails).stream().map(FlagDetail::getFlagCode)
            .toList();
    }

    private static boolean isLanguageInterpreterFlag(String flag) {
        return flag.equals(LANGUAGE_INTERPRETER_FLAG) || flag.equals(SIGN_LANGUAGE_INTERPRETER_FLAG);
    }

    public static String getOtherReasonableAdjustmentDetails(List<FlagDetail> flagDetails) {
        String details = null;
        List<FlagDetail> flags = new ArrayList<>();
        List<FlagDetail> reasonableAdjustmentFlags = getReasonableAdjustmentFlags(flagDetails);
        List<FlagDetail> languageInterpreterFlags = getSpokenLanguageInterpreterFlags(flagDetails);

        flags.addAll(reasonableAdjustmentFlags);
        flags.addAll(languageInterpreterFlags);

        for (FlagDetail flagDetail: flags) {
            String raDetails = Stream.of(
                    flagDetail.getFlagCode(),
                    flagDetail.getName(),
                    flagDetail.getFlagComment(),
                    isLanguageInterpreterFlag(flagDetail.getFlagCode()) ? flagDetail.getSubTypeValue() : null
                )
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(": "));

            details = Stream.of(details, raDetails)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(", "));
        }

        return details;
    }
}
