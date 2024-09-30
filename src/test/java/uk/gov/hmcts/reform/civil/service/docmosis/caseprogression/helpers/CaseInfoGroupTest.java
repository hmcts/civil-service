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
        SolicitorReferences solicitorReferences = SolicitorReferences.builder()
            .applicantSolicitor1Reference("ClaimantRef")
            .respondentSolicitor1Reference("DefendantRef")
            .build();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .respondent1(respondent1)
            .respondent2(respondent2)
            .solicitorReferences(solicitorReferences)
            .caseNameHmctsInternal("Test Case Name")
            .build();

        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();
        builder = caseInfoPopulator.populateCaseInfo(builder, caseData);

        Assertions.assertEquals("1234567890123456", builder.build().getCaseNumber());
        Assertions.assertEquals("Claimant 1", builder.build().getClaimantNum());
        Assertions.assertEquals("Defendant 1", builder.build().getDefendantNum());
        Assertions.assertEquals("Test Case Name", builder.build().getCaseName());
        Assertions.assertEquals("ClaimantRef", builder.build().getClaimantReference());
        Assertions.assertEquals("DefendantRef", builder.build().getDefendantReference());
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

        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();

        builder = caseInfoPopulator.populateCaseInfo(builder, caseData);

        Assertions.assertEquals("1234567890123456", builder.build().getCaseNumber());
        Assertions.assertNull(builder.build().getClaimant2Name());
        Assertions.assertNull(builder.build().getDefendant2Name());
        Assertions.assertEquals("Claimant", builder.build().getClaimantNum());
        Assertions.assertEquals("Defendant", builder.build().getDefendantNum());
        Assertions.assertEquals("Test Case Name", builder.build().getCaseName());
        Assertions.assertNull(builder.build().getClaimantReference());
        Assertions.assertNull(builder.build().getDefendantReference());
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

        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();

        builder = caseInfoPopulator.populateCaseInfo(builder, caseData);

        assertNull(builder.build().getClaimantReference());
        assertNull(builder.build().getDefendantReference());
    }

}
