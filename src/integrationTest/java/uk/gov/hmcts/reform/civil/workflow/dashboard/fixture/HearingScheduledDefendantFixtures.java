package uk.gov.hmcts.reform.civil.workflow.dashboard.fixture;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.util.List;

public final class HearingScheduledDefendantFixtures {

    private static final String HEARING_SCHEDULED_DEFENDANT = "hearing-scheduled-defendant";
    private static final String CASE_ID = "8123456783";

    private HearingScheduledDefendantFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(HEARING_SCHEDULED_DEFENDANT);
    }

    public static List<LocationRefData> locations() {
        return List.of(new LocationRefData().setSiteName("Name").setCourtAddress("Loc").setPostcode("1"));
    }

    public static String caseReference() {
        return CASE_ID;
    }
}
