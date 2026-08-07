package uk.gov.hmcts.reform.civil.workflow.ga.fixture;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

public final class GaCreationFixtures {

    public static final long CASE_ID = 1234567890123456L;
    public static final String APPLICANT_USER_ID = "applicant-solicitor-id";
    public static final String RESPONDENT_USER_ID = "respondent-solicitor-id";
    public static final String APPLICANT_EMAIL = "applicant.solicitor@example.com";
    public static final String RESPONDENT_EMAIL = "respondent.solicitor@example.com";
    public static final String APPLICANT_ORGANISATION = "applicant-organisation";
    public static final String RESPONDENT_ORGANISATION = "respondent-organisation";
    public static final LocalDateTime SUBMITTED_DATE = LocalDateTime.of(2026, 7, 1, 9, 30);
    public static final LocalDateTime APPLICATION_DEADLINE = LocalDateTime.of(2026, 8, 14, 23, 59);
    public static final LocalDateTime CLAIM_DISMISSED_DEADLINE = LocalDateTime.of(2029, 8, 7, 23, 59);
    public static final Fee APPLICATION_FEE = new Fee()
        .setCode("FEE0442")
        .setVersion("1")
        .setCalculatedAmountInPence(BigDecimal.valueOf(27500));

    private GaCreationFixtures() {
    }

    public static CaseData applicationInput() {
        DynamicList preferredLocations = fromList(List.of(
            "Central London County Court - Thomas More Building - EC4A 3TR"
        ));
        preferredLocations.setValue(preferredLocations.getListItems().getFirst());

        return CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .ccdState(CaseState.CASE_ISSUED)
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .caseNameHmctsInternal("Applicant Limited v Respondent Limited")
            .submittedDate(SUBMITTED_DATE)
            .solicitorReferences(new SolicitorReferences()
                                     .setApplicantSolicitor1Reference("APP-REF")
                                     .setRespondentSolicitor1Reference("RESP-REF"))
            .applicant1(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant Limited"))
            .respondent1(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent Limited"))
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .applicantSolicitor1UserDetails(new IdamUserDetails()
                                                .setId(APPLICANT_USER_ID)
                                                .setEmail(APPLICANT_EMAIL))
            .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL)
            .applicant1OrganisationPolicy(organisationPolicy(
                APPLICANT_ORGANISATION,
                CaseRole.APPLICANTSOLICITORONE
            ))
            .respondent1OrganisationPolicy(organisationPolicy(
                RESPONDENT_ORGANISATION,
                CaseRole.RESPONDENTSOLICITORONE
            ))
            .caseManagementLocation(new CaseLocationCivil().setRegion("1").setBaseLocation("123456"))
            .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.EXTEND_TIME)))
            .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(YesOrNo.NO))
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YesOrNo.YES))
            .generalAppUrgencyRequirement(new GAUrgencyRequirement().setGeneralAppUrgency(YesOrNo.NO))
            .generalAppStatementOfTruth(new GAStatementOfTruth()
                                            .setName("Applicant Solicitor")
                                            .setRole("Solicitor"))
            .generalAppPBADetails(new GAPbaDetails().setFee(APPLICATION_FEE))
            .generalAppDetailsOfOrder("Extend the deadline by 14 days")
            .generalAppReasonsOfOrder("The parties require additional time")
            .generalAppHearingDetails(new GAHearingDetails()
                                          .setHearingYesorNo(YesOrNo.NO)
                                          .setHearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .setHearingPreferredLocation(preferredLocations))
            .generalAppEvidenceDocument(List.of(element(new Document()
                                                            .setDocumentUrl("http://dm-store/evidence")
                                                            .setDocumentBinaryUrl("http://dm-store/evidence/binary")
                                                            .setDocumentFileName("supporting-evidence.pdf"))))
            .build();
    }

    private static OrganisationPolicy organisationPolicy(String organisationId, CaseRole caseRole) {
        return new OrganisationPolicy()
            .setOrganisation(new Organisation().setOrganisationID(organisationId))
            .setOrgPolicyCaseAssignedRole(caseRole.getFormattedName());
    }
}
