package uk.gov.hmcts.reform.civil.service.robotics.utils;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.NoticeOfChange;
import uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_DATE;

public class RoboticsDataUtil {

    public static final String APPLICANT_SOLICITOR_ID = "001";
    public static final String RESPONDENT_SOLICITOR_ID = "002";
    public static final String APPLICANT_ID = "001";
    public static final String RESPONDENT_ID = "002";
    public static final String RESPONDENT2_ID = "003";
    public static final String APPLICANT2_ID = "004";
    public static final String RESPONDENT2_SOLICITOR_ID = "003";

    private RoboticsDataUtil() {
        // NoOp
    }

    private static NoticeOfChange getLatestChange(OrganisationPolicy orgPolicy, String litigantCode) {
        var latestChange = OrgPolicyUtils.getLatestOrganisationChanges(orgPolicy);
        if (latestChange != null) {
            return new NoticeOfChange()
                .setLitigiousPartyID(litigantCode)
                .setDateOfNoC(latestChange.getToTimestamp().format(ISO_DATE));
        } else {
            return null;
        }
    }

    public static List<NoticeOfChange> buildNoticeOfChange(CaseData caseData) {
        var latestChanges =  Arrays.asList(
                getLatestChange(caseData.getApplicant1OrganisationPolicy(), APPLICANT_SOLICITOR_ID),
                getLatestChange(caseData.getRespondent1OrganisationPolicy(), RESPONDENT_ID),
                getLatestChange(caseData.getRespondent2OrganisationPolicy(), RESPONDENT2_ID)
            )
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return latestChanges.size() > 0 ? latestChanges : null;
    }

    /**
     * Continuous-feed RPA must not transfer Breathing Space. Returns a copy with
     * {@code breathing} cleared so event history / top-level BS are omitted without
     * changing notifier or mapper APIs. Handed-offline / adhoc keep the original case data.
     */
    public static CaseData withoutBreathingSpace(CaseData caseData) {
        if (caseData == null || caseData.getBreathing() == null) {
            return caseData;
        }
        return caseData.toBuilder().breathing(null).build();
    }

}
