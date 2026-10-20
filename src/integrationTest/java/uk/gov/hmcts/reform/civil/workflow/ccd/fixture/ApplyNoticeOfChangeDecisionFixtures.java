package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.LocalDateTime;
import java.util.List;

public final class ApplyNoticeOfChangeDecisionFixtures {

    private static final String NOC_CLAIM_ISSUED = "noc-claim-issued";
    private static final String NEW_ORG_ID = "NEW-ORG-001";
    private static final LocalDateTime REQUEST_TIMESTAMP = LocalDateTime.of(2026, 6, 15, 10, 0);

    private ApplyNoticeOfChangeDecisionFixtures() {
    }

    public static CaseData respondent1SolicitorChange() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "changeOrganisationRequestField", buildChangeOrgRequest(
                CaseRole.RESPONDENTSOLICITORONE.getFormattedName(),
                NEW_ORG_ID,
                "QWERTY-R1"
            ));
            CaseDataTemplates.set(template, "addLegalRepDeadlineRes1", LocalDateTime.of(2026, 7, 1, 16, 0));
        });
    }

    public static CaseData applicantSolicitorChange() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "changeOrganisationRequestField", buildChangeOrgRequest(
                CaseRole.APPLICANTSOLICITORONE.getFormattedName(),
                NEW_ORG_ID,
                "QWERTY-A"
            ));
        });
    }

    public static CaseData lipToSolicitorChange() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "respondent1Represented", YesOrNo.NO);
            CaseDataTemplates.set(template, "defendantUserDetails",
                new IdamUserDetails().setEmail("defendant@example.com").setId("def-user-id"));
            CaseDataTemplates.set(template, "respondent1OrganisationPolicy", null);

            ChangeOrganisationRequest request = new ChangeOrganisationRequest();
            request.setCaseRoleId(buildCaseRoleId(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()));
            request.setOrganisationToAdd(new Organisation().setOrganisationID(NEW_ORG_ID));
            request.setRequestTimestamp(REQUEST_TIMESTAMP);
            request.setCreatedBy("newsolicitor@example.com");
            CaseDataTemplates.set(template, "changeOrganisationRequestField", request);
        });
    }

    public static CaseData missingChangeOrganisationRequest() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED);
    }

    private static ChangeOrganisationRequest buildChangeOrgRequest(String caseRole,
                                                                    String orgToAddId,
                                                                    String orgToRemoveId) {
        ChangeOrganisationRequest request = new ChangeOrganisationRequest();
        request.setCaseRoleId(buildCaseRoleId(caseRole));
        request.setOrganisationToAdd(new Organisation().setOrganisationID(orgToAddId));
        request.setOrganisationToRemove(new Organisation().setOrganisationID(orgToRemoveId));
        request.setRequestTimestamp(REQUEST_TIMESTAMP);
        request.setCreatedBy("newsolicitor@example.com");
        return request;
    }

    private static DynamicList buildCaseRoleId(String caseRole) {
        DynamicListElement element = DynamicListElement.dynamicElementFromCode(caseRole, caseRole);
        return new DynamicList(element, List.of(element));
    }
}
