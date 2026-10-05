package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.LocalDateTime;

public final class UpdateCaseDetailsAfterNoCFixtures {

    private static final String NOC_CLAIM_ISSUED = "noc-claim-issued";
    private static final String NEW_ORG_ID = "NEW-ORG-001";
    private static final String NEW_SOLICITOR_EMAIL = "newsolicitor@example.com";
    private static final LocalDateTime TIMESTAMP = LocalDateTime.of(2026, 6, 15, 10, 0);

    private UpdateCaseDetailsAfterNoCFixtures() {
    }

    public static CaseData respondent1SolicitorReplaced() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "changeOfRepresentation", buildChangeOfRepresentation(
                CaseRole.RESPONDENTSOLICITORONE.getFormattedName(),
                NEW_ORG_ID,
                "QWERTY-R1",
                "respondent1solicitor@example.com",
                "RES1-REF-001"
            ));
            CaseDataTemplates.set(template, "changeOrganisationRequestField", buildMinimalChangeOrgRequest());
        });
    }

    public static CaseData applicantSolicitorReplaced() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "changeOfRepresentation", buildChangeOfRepresentation(
                CaseRole.APPLICANTSOLICITORONE.getFormattedName(),
                NEW_ORG_ID,
                "QWERTY-A",
                "applicantsolicitor@example.com",
                "APP-REF-001"
            ));
            CaseDataTemplates.set(template, "changeOrganisationRequestField", buildMinimalChangeOrgRequest());
        });
    }

    public static CaseData lipDefendantGainingSolicitor() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "respondent1Represented", YesOrNo.NO);
            CaseDataTemplates.set(template, "defendantUserDetails",
                new IdamUserDetails().setEmail("defendant@example.com").setId("def-user-id"));
            CaseDataTemplates.set(template, "changeOfRepresentation", buildChangeOfRepresentation(
                CaseRole.RESPONDENTSOLICITORONE.getFormattedName(),
                NEW_ORG_ID,
                null,
                null,
                null
            ));
            CaseDataTemplates.set(template, "changeOrganisationRequestField", buildMinimalChangeOrgRequest());
        });
    }

    public static CaseData lipClaimantGainingSolicitor() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "applicant1Represented", YesOrNo.NO);
            CaseDataTemplates.set(template, "claimantUserDetails",
                new IdamUserDetails().setEmail("claimant@example.com").setId("claimant-user-id"));
            CaseDataTemplates.set(template, "changeOfRepresentation", buildChangeOfRepresentation(
                CaseRole.APPLICANTSOLICITORONE.getFormattedName(),
                NEW_ORG_ID,
                null,
                null,
                null
            ));
            CaseDataTemplates.set(template, "changeOrganisationRequestField", buildMinimalChangeOrgRequest());
        });
    }

    public static CaseData missingChangeOfRepresentation() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "changeOrganisationRequestField", buildMinimalChangeOrgRequest());
        });
    }

    public static CaseData respondent1SolicitorReplacedUnspec() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "CaseAccessCategory", CaseCategory.UNSPEC_CLAIM);
            CaseDataTemplates.set(template, "changeOfRepresentation", buildChangeOfRepresentation(
                CaseRole.RESPONDENTSOLICITORONE.getFormattedName(),
                NEW_ORG_ID,
                "QWERTY-R1",
                "respondent1solicitor@example.com",
                "RES1-REF-001"
            ));
            CaseDataTemplates.set(template, "changeOrganisationRequestField", buildMinimalChangeOrgRequest());
        });
    }

    private static ChangeOfRepresentation buildChangeOfRepresentation(String caseRole,
                                                                       String orgToAddId,
                                                                       String orgToRemoveId,
                                                                       String formerEmail,
                                                                       String formerReference) {
        return new ChangeOfRepresentation()
            .setCaseRole(caseRole)
            .setOrganisationToAddID(orgToAddId)
            .setOrganisationToRemoveID(orgToRemoveId)
            .setTimestamp(TIMESTAMP)
            .setFormerRepresentationEmailAddress(formerEmail)
            .setFormerRepresentationReference(formerReference);
    }

    private static ChangeOrganisationRequest buildMinimalChangeOrgRequest() {
        ChangeOrganisationRequest request = new ChangeOrganisationRequest();
        request.setCreatedBy(NEW_SOLICITOR_EMAIL);
        request.setOrganisationToAdd(new Organisation().setOrganisationID(
            "org id to persist updated change organisation request field"));
        return request;
    }
}
