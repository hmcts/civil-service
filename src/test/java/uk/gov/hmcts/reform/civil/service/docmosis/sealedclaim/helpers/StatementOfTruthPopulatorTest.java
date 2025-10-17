package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class StatementOfTruthPopulatorTest {

    private static final ObjectMapper GA_OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module());
    private final GaCaseDataEnricher gaCaseDataEnricher = new GaCaseDataEnricher();

    @InjectMocks
    private StatementOfTruthPopulator statementOfTruthPopulator;

    @Mock
    private FeatureToggleService featureToggleService;

    @Test
    void shouldPopulateDetailsForRespondent1() {
        StatementOfTruth respondent1StatementOfTruth = StatementOfTruth.builder().name("Respondent 1").build();
        Respondent1DQ respondent1DQ = Respondent1DQ.builder()
            .respondent1DQStatementOfTruth(respondent1StatementOfTruth)
            .build();

        LocalDateTime respondent1ResponseDate = LocalDateTime.now();
        CaseData caseData = gaCaseData(respondent1DQ, null, respondent1ResponseDate, null, null);

        SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder = SealedClaimResponseFormForSpec.builder();

        given(featureToggleService.isCarmEnabledForCase(caseData)).willReturn(false);

        statementOfTruthPopulator.populateStatementOfTruthDetails(builder, caseData);

        SealedClaimResponseFormForSpec form = builder.build();
        assertEquals(respondent1StatementOfTruth, form.getStatementOfTruth());
        assertFalse(form.isCheckCarmToggle());
        verify(featureToggleService).isCarmEnabledForCase(caseData);
    }

    @Test
    void shouldPopulateDetailsForRespondent2() {
        StatementOfTruth respondent2StatementOfTruth = StatementOfTruth.builder().name("Respondent 2").build();
        Respondent1DQ respondent1DQ = Respondent1DQ.builder()
            .respondent1DQRequestedCourt(RequestedCourt.builder().build())
            .build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder()
            .respondent2DQRequestedCourt(RequestedCourt.builder().build())
            .respondent2DQStatementOfTruth(respondent2StatementOfTruth)
            .build();

        LocalDateTime respondent2ResponseDate = LocalDateTime.now();
        LocalDateTime respondent1ResponseDate = respondent2ResponseDate.minusDays(1);
        CaseData caseData = gaCaseData(respondent1DQ, respondent2DQ, respondent1ResponseDate, respondent2ResponseDate, null);

        SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder = SealedClaimResponseFormForSpec.builder();

        given(featureToggleService.isCarmEnabledForCase(caseData)).willReturn(true);

        statementOfTruthPopulator.populateStatementOfTruthDetails(builder, caseData);

        SealedClaimResponseFormForSpec form = builder.build();
        assertEquals(respondent2StatementOfTruth, form.getStatementOfTruth());
        assertTrue(form.isCheckCarmToggle());
        verify(featureToggleService).isCarmEnabledForCase(caseData);
    }

    @Test
    void shouldHandleNoStatementOfTruthForRespondent1AndRespondent2() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQRequestedCourt(RequestedCourt.builder().build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQRequestedCourt(RequestedCourt.builder().build()).build();

        LocalDateTime respondent1ResponseDate = LocalDateTime.now();
        LocalDateTime respondent2ResponseDate = respondent1ResponseDate.plusDays(1);
        CaseData caseData = gaCaseData(respondent1DQ, respondent2DQ, respondent1ResponseDate, respondent2ResponseDate, null);

        SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder = SealedClaimResponseFormForSpec.builder();

        given(featureToggleService.isCarmEnabledForCase(caseData)).willReturn(false);

        statementOfTruthPopulator.populateStatementOfTruthDetails(builder, caseData);

        SealedClaimResponseFormForSpec form = builder.build();
        assertNull(form.getStatementOfTruth());
        assertFalse(form.isCheckCarmToggle());
        verify(featureToggleService).isCarmEnabledForCase(caseData);
    }

    @Test
    void shouldHandleCarmFeatureToggleEnabled() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQRequestedCourt(RequestedCourt.builder().build()).build();
        CaseData caseData = gaCaseData(respondent1DQ, null, null, null, null);

        SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder = SealedClaimResponseFormForSpec.builder();

        given(featureToggleService.isCarmEnabledForCase(caseData)).willReturn(true);

        statementOfTruthPopulator.populateStatementOfTruthDetails(builder, caseData);

        SealedClaimResponseFormForSpec form = builder.build();
        Assertions.assertTrue(form.isCheckCarmToggle());
        verify(featureToggleService).isCarmEnabledForCase(caseData);
    }

    @Test
    void shouldHandleCarmFeatureToggleDisabled() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQRequestedCourt(RequestedCourt.builder().build()).build();
        CaseData caseData = gaCaseData(respondent1DQ, null, null, null, YesOrNo.YES);

        SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder = SealedClaimResponseFormForSpec.builder();

        given(featureToggleService.isCarmEnabledForCase(caseData)).willReturn(false);
        given(featureToggleService.isPinInPostEnabled()).willReturn(false);

        statementOfTruthPopulator.populateStatementOfTruthDetails(builder, caseData);

        SealedClaimResponseFormForSpec form = builder.build();
        Assertions.assertFalse(form.isCheckCarmToggle());
        Assertions.assertEquals(YesOrNo.YES,
                                form.getMediation());
        verify(featureToggleService).isCarmEnabledForCase(caseData);
    }

    private CaseData gaCaseData(Respondent1DQ respondent1DQ,
                                Respondent2DQ respondent2DQ,
                                LocalDateTime respondent1ResponseDate,
                                LocalDateTime respondent2ResponseDate,
                                YesOrNo mediation) {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withCcdCaseReference(CaseDataBuilder.CASE_ID)
            .withGeneralAppParentCaseReference(CaseDataBuilder.PARENT_CASE_ID)
            .withLocationName("Nottingham County Court and Family Court (and Crown)")
            .withGaCaseManagementLocation(GACaseLocation.builder()
                                              .siteName("testing")
                                              .address("london court")
                                              .baseLocation("2")
                                              .postcode("BA 117")
                                              .build())
            .build();

        CaseData converted = GA_OBJECT_MAPPER.convertValue(gaCaseData, CaseData.class);
        CaseData enriched = gaCaseDataEnricher.enrich(converted, gaCaseData);

        CaseData.CaseDataBuilder<?, ?> builder = enriched.toBuilder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .respondent1ResponseDate(respondent1ResponseDate)
            .respondent2ResponseDate(respondent2ResponseDate);

        if (mediation != null) {
            builder.responseClaimMediationSpecRequired(mediation);
        }

        return builder.build();
    }
}
