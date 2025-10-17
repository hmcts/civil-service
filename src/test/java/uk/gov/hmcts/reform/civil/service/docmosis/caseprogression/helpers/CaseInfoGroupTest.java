package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.Party.Type;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;

import static org.junit.Assert.assertNull;

@ExtendWith(SpringExtension.class)
public class CaseInfoGroupTest {

    private static final ObjectMapper GA_OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module());
    private static final GaCaseDataEnricher GA_CASE_DATA_ENRICHER = new GaCaseDataEnricher();

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
        CaseData caseData = gaCaseData(builder -> builder
            .ccdCaseReference(1234567890123456L)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .respondent1(respondent1)
            .respondent2(respondent2)
            .solicitorReferences(solicitorReferences)
            .caseNameHmctsInternal("Test Case Name"));

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
        CaseData caseData = gaCaseData(builder -> builder
            .ccdCaseReference(1234567890123456L)
            .applicant1(applicant1)
            .respondent1(respondent1)
            .caseNameHmctsInternal("Test Case Name"));

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
        CaseData caseData = gaCaseData(builder -> builder
            .ccdCaseReference(1234567890123456L)
            .applicant1(applicant1)
            .respondent1(respondent1)
            .caseNameHmctsInternal("Test Case Name"));

        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();

        builder = caseInfoPopulator.populateCaseInfo(builder, caseData);

        assertNull(builder.build().getClaimantReference());
        assertNull(builder.build().getDefendantReference());
    }

    private CaseData gaCaseData(UnaryOperator<CaseData.CaseDataBuilder<?, ?>> customiser) {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withCcdCaseReference(CaseDataBuilder.CASE_ID)
            .withGeneralAppParentCaseReference(CaseDataBuilder.PARENT_CASE_ID)
            .withLocationName("Nottingham County Court and Family Court (and Crown)")
            .withGaCaseManagementLocation(GACaseLocation.builder()
                                              .siteName("testing")
                                              .address("london court")
                                              .baseLocation("000000")
                                              .postcode("BA 117")
                                              .build())
            .build();

        CaseData converted = GA_OBJECT_MAPPER.convertValue(gaCaseData, CaseData.class);
        CaseData enriched = GA_CASE_DATA_ENRICHER.enrich(converted, gaCaseData);

        return customiser.apply(enriched.toBuilder()).build();
    }
}
