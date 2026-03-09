package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.Party.Type;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;

import static org.junit.Assert.assertNull;

@ExtendWith(SpringExtension.class)
public class CaseInfoGroupTest {

    @InjectMocks
    private CaseInfoPopulator caseInfoPopulator;

    @Test
    void shouldPopulateCaseInfo_WhenAllFieldsPresent() {
        Party applicant1 = Party.builder().partyName("Claimant 1").type(Type.COMPANY).build();
        Party applicant2 = Party.builder().partyName("Claimant 2").type(Type.COMPANY).build();
        Party respondent1 = Party.builder().partyName("Defendant 1").type(Type.COMPANY).build();
        Party respondent2 = Party.builder().partyName("Defendant 2").type(Type.COMPANY).build();
        SolicitorReferences solicitorReferences = new SolicitorReferences()
            .setApplicantSolicitor1Reference("ClaimantRef")
            .setRespondentSolicitor1Reference("DefendantRef");
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .respondent1(respondent1)
            .respondent2(respondent2)
            .solicitorReferences(solicitorReferences)
            .caseNameHmctsInternal("Test Case Name")
            .build();

        JudgeFinalOrderForm form = new JudgeFinalOrderForm();
        form = caseInfoPopulator.populateCaseInfo(form, caseData);

        Assertions.assertEquals("1234567890123456", form.getCaseNumber());
        Assertions.assertEquals("Claimant 1", form.getClaimantNum());
        Assertions.assertEquals("Defendant 1", form.getDefendantNum());
        Assertions.assertEquals("Test Case Name", form.getCaseName());
        Assertions.assertEquals("ClaimantRef", form.getClaimantReference());
        Assertions.assertEquals("DefendantRef", form.getDefendantReference());
    }

    @Test
    void shouldPopulateCaseInfo_WhenOnlySingleClaimantAndDefendantPresent() {
        Party applicant1 = Party.builder().partyName("Claimant 1").type(Type.COMPANY).build();
        Party respondent1 = Party.builder().partyName("Defendant 1").type(Type.COMPANY).build();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .applicant1(applicant1)
            .respondent1(respondent1)
            .caseNameHmctsInternal("Test Case Name")
            .build();

        JudgeFinalOrderForm form = new JudgeFinalOrderForm();

        form = caseInfoPopulator.populateCaseInfo(form, caseData);

        Assertions.assertEquals("1234567890123456", form.getCaseNumber());
        Assertions.assertNull(form.getClaimant2Name());
        Assertions.assertNull(form.getDefendant2Name());
        Assertions.assertEquals("Claimant", form.getClaimantNum());
        Assertions.assertEquals("Defendant", form.getDefendantNum());
        Assertions.assertEquals("Test Case Name", form.getCaseName());
        Assertions.assertNull(form.getClaimantReference());
        Assertions.assertNull(form.getDefendantReference());
    }

    @Test
    void shouldPopulateCaseInfo_WhenSolicitorReferencesAreAbsent() {
        Party applicant1 = Party.builder().partyName("Claimant 1").type(Type.COMPANY).build();
        Party respondent1 = Party.builder().partyName("Defendant 1").type(Type.COMPANY).build();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .applicant1(applicant1)
            .respondent1(respondent1)
            .caseNameHmctsInternal("Test Case Name")
            .build();

        JudgeFinalOrderForm form = new JudgeFinalOrderForm();

        form = caseInfoPopulator.populateCaseInfo(form, caseData);

        assertNull(form.getClaimantReference());
        assertNull(form.getDefendantReference());
    }

}
