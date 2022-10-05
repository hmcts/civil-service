package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class CaseListSolicitorReferenceUtils {

    private CaseListSolicitorReferenceUtils() {
        //NO-OP
    }

    public static String getAllDefendantSolicitorReferences(CaseData caseData) {
        return getCommaSeparatedString(
            Arrays.asList(
                caseData.getSolicitorReferences() != null
                    ? ofNullable(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
                        .map(Object::toString).orElse(null) : null,
                YES.equals(caseData.getAddRespondent2()) && NO.equals(caseData.getRespondent2SameLegalRepresentative())
                   ? ofNullable(caseData.getRespondentSolicitor2Reference())
                        .map(Object::toString).orElse(null) : null));
    }

    public static String getAllDefendantSolicitorReferences(String solicitor1Reference, String solicitor2Reference) {
        return getCommaSeparatedString(Arrays.asList(solicitor1Reference, solicitor2Reference));
    }

    public static String getAllOrganisationPolicyReferences(CaseData caseData) {
        return getCommaSeparatedString(
            Arrays.asList(
                ofNullable(caseData.getApplicant1OrganisationPolicy().getOrgPolicyReference())
                    .map(Object::toString).orElse(null),
                caseData.getRespondent1OrganisationPolicy() != null
                    ? ofNullable(caseData.getRespondent1OrganisationPolicy().getOrgPolicyReference())
                    .map(Object::toString).orElse(null) : null,
                YES.equals(caseData.getAddRespondent2()) && NO.equals(caseData.getRespondent2SameLegalRepresentative())
                    && caseData.getRespondent2OrganisationPolicy() != null
                    ? ofNullable(caseData.getRespondent2OrganisationPolicy().getOrgPolicyReference())
                    .map(Object::toString).orElse(null) : null));
    }

    private static String getCommaSeparatedString(List<String> values) {
        return values.stream()
            .filter(s -> s != null && !s.isEmpty())
            .collect(Collectors.joining(", "));
    }
}
