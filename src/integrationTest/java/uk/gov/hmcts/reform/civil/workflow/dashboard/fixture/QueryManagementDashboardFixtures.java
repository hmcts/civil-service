package uk.gov.hmcts.reform.civil.workflow.dashboard.fixture;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

public final class QueryManagementDashboardFixtures {

    private static final String TEMPLATE = "query-management-start";
    private static final long CASE_ID = 7201459990001L;

    private QueryManagementDashboardFixtures() {
    }

    public static CaseData lipClaimantCaseWithQueryResponse() {
        return CaseDataTemplates.load(TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdCaseReference", CASE_ID);
            CaseDataTemplates.set(template, "applicant1Represented", YesOrNo.NO);
            CaseDataTemplates.set(template, "respondent1Represented", YesOrNo.YES);
            CaseDataTemplates.set(template, "applicant1", applicantParty());
            CaseDataTemplates.set(template, "respondent1", respondentParty());
            CaseDataTemplates.set(template, "queries", queriesWithResponse());
        });
    }

    public static CaseData lipDefendantCaseWithQueryResponse() {
        return CaseDataTemplates.load(TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdCaseReference", CASE_ID);
            CaseDataTemplates.set(template, "applicant1Represented", YesOrNo.YES);
            CaseDataTemplates.set(template, "respondent1Represented", YesOrNo.NO);
            CaseDataTemplates.set(template, "applicant1", applicantParty());
            CaseDataTemplates.set(template, "respondent1", respondentParty());
            CaseDataTemplates.set(template, "queries", queriesWithResponse());
        });
    }

    public static String caseReference() {
        return Long.toString(CASE_ID);
    }

    private static CaseQueriesCollection queriesWithResponse() {
        String rootId = UUID.randomUUID().toString();

        CaseMessage rootQuery = new CaseMessage();
        rootQuery.setId(rootId);
        rootQuery.setSubject("Test query");
        rootQuery.setName("Party");
        rootQuery.setBody("Question about the case");
        rootQuery.setCreatedBy("party-user-id");
        rootQuery.setCreatedOn(OffsetDateTime.now().minusHours(2));
        rootQuery.setIsHearingRelated(YesOrNo.NO);

        CaseMessage response = new CaseMessage();
        response.setId(UUID.randomUUID().toString());
        response.setSubject("Test query");
        response.setName("Caseworker");
        response.setBody("Court response");
        response.setParentId(rootId);
        response.setCreatedBy("caseworker-id");
        response.setCreatedOn(OffsetDateTime.now());
        response.setIsHearingRelated(YesOrNo.NO);

        CaseQueriesCollection queries = new CaseQueriesCollection();
        queries.setPartyName("All queries");
        queries.setCaseMessages(new ArrayList<>(List.of(element(rootQuery), element(response))));
        return queries;
    }

    private static Object applicantParty() {
        return java.util.Map.of(
            "type", "INDIVIDUAL",
            "individualFirstName", "Claimant",
            "individualLastName", "Tester"
        );
    }

    private static Object respondentParty() {
        return java.util.Map.of(
            "type", "INDIVIDUAL",
            "individualFirstName", "Defendant",
            "individualLastName", "Tester"
        );
    }
}
