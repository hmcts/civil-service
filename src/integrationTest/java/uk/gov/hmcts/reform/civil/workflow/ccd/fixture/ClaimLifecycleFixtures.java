package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

public final class ClaimLifecycleFixtures {

    public static final long CASE_REFERENCE = 1779198401913981L;
    public static final String CLAIM_REFERENCE = "000DC001";
    public static final String RESPONDENT_ONE_ORGANISATION = "QWERTY R";
    public static final String RESPONDENT_TWO_ORGANISATION = "QWERTY R2";
    public static final String DEFENDANT_ONE_OPTION = "Defendant One: Mr. John Rambo";

    // State-flow predicates compare lifecycle dates with the real current date, so keep the callback safely ahead of it.
    public static final LocalDateTime CALLBACK_TIME = LocalDate.now()
        .plusYears(2)
        .atTime(10, 15);

    private ClaimLifecycleFixtures() {
    }

    public static CertificateOfService certificateOfService(
        String fileName,
        LocalDate dateOfService,
        LocalDate deemedDateOfService
    ) {
        Document evidence = new Document()
            .setDocumentUrl("http://dm-store/documents/" + fileName)
            .setDocumentBinaryUrl("http://dm-store/documents/" + fileName + "/binary")
            .setDocumentFileName(fileName);

        return new CertificateOfService()
            .setCosDateOfServiceForDefendant(dateOfService)
            .setCosDateDeemedServedForDefendant(deemedDateOfService)
            .setCosEvidenceDocument(List.of(element(evidence)));
    }

    static CaseData withIssuedLifecycleDates(CaseData caseData) {
        return caseData.toBuilder()
            .ccdCaseReference(CASE_REFERENCE)
            .legacyCaseReference(CLAIM_REFERENCE)
            .submittedDate(CALLBACK_TIME.minusMonths(1))
            .paymentSuccessfulDate(CALLBACK_TIME.minusWeeks(3))
            .issueDate(CALLBACK_TIME.minusWeeks(2).toLocalDate())
            .build();
    }

    static OrganisationPolicy registeredPolicy(String organisationId, CaseRole caseRole, String reference) {
        return policy(caseRole, reference)
            .setOrganisation(new Organisation().setOrganisationID(organisationId));
    }

    static OrganisationPolicy clearedPolicy(CaseRole caseRole) {
        return policy(caseRole, caseRole.name() + "-policy")
            .setOrganisation(new Organisation());
    }

    static StatementOfTruth statementOfTruth() {
        return new StatementOfTruth()
            .setName("Applicant Solicitor")
            .setRole("Solicitor");
    }

    private static OrganisationPolicy policy(CaseRole caseRole, String reference) {
        return new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole(caseRole.getFormattedName())
            .setOrgPolicyReference(reference);
    }
}
