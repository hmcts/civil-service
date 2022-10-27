package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisation;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisationCollectionItem;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class OrgPolicyUtils {

    private OrgPolicyUtils() {
        //NO-OP
    }

    public static String getRespondent1SolicitorOrgId(CaseData caseData) {
        String orgId = getOrgId(caseData.getRespondent1OrganisationPolicy());
        return orgId != null ? orgId : caseData.getRespondent1OrganisationIDCopy();
    }

    public static String getRespondent2SolicitorOrgId(CaseData caseData) {
        String orgId = getOrgId(caseData.getRespondent2OrganisationPolicy());
        return orgId != null ? orgId : caseData.getRespondent2OrganisationIDCopy();
    }

    private static String getOrgId(OrganisationPolicy orgPolicy) {
        return orgPolicy != null
            && orgPolicy.getOrganisation() != null
            && orgPolicy.getOrganisation().getOrganisationID() != null
            ? orgPolicy.getOrganisation().getOrganisationID() : null;
    }

    private static Comparator<PreviousOrganisationCollectionItem> sortPreviousOrgs() {
        return (nocEvent1, nocEvent2) -> {
            if (nocEvent1.getValue().getToTimestamp().isAfter(nocEvent2.getValue().getToTimestamp())) {
                return -1;
            } else if (nocEvent1.getValue().getToTimestamp().isBefore(nocEvent2.getValue().getToTimestamp())) {
                return 1;
            } else {
                return 0;
            }
        };
    }

    private static List<PreviousOrganisationCollectionItem> getPreviousOrganisations(OrganisationPolicy orgPolicy) {
        return orgPolicy != null
            && orgPolicy.getPreviousOrganisations() != null
            ? orgPolicy.getPreviousOrganisations() : new ArrayList<>();
    }

    public static PreviousOrganisation getLatestNoCEvent(CaseData caseData) {
        var mostRecentOrganisation = Stream.of(
                getPreviousOrganisations(caseData.getApplicant1OrganisationPolicy()),
                getPreviousOrganisations(caseData.getRespondent1OrganisationPolicy()),
                getPreviousOrganisations(caseData.getRespondent2OrganisationPolicy())
            ).flatMap(Collection::stream)
            .sorted(sortPreviousOrgs())
            .findFirst();
        return mostRecentOrganisation.isPresent() ? mostRecentOrganisation.get().getValue() : null;
    }
}
