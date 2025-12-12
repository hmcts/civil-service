package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class StatementOfTruthPopulatorTest {

    @InjectMocks
    private StatementOfTruthPopulator statementOfTruthPopulator;

    @Mock
    private FeatureToggleService featureToggleService;

    @Test
    void shouldPopulateDetailsForRespondent1() {
        StatementOfTruth respondent1StatementOfTruth = new StatementOfTruth();
        respondent1StatementOfTruth.setName("Respondent 1");
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQStatementOfTruth(respondent1StatementOfTruth);

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1DQ(respondent1DQ)
            .respondent1ResponseDate(LocalDateTime.now())
            .build();

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
        StatementOfTruth respondent2StatementOfTruth = new StatementOfTruth();
        respondent2StatementOfTruth.setName("Respondent 2");
        RequestedCourt requestedCourt1 = new RequestedCourt();
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQRequestedCourt(requestedCourt1);
        RequestedCourt requestedCourt2 = new RequestedCourt();
        Respondent2DQ respondent2DQ = new Respondent2DQ();
        respondent2DQ.setRespondent2DQRequestedCourt(requestedCourt2);
        respondent2DQ.setRespondent2DQStatementOfTruth(respondent2StatementOfTruth);

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .respondent2ResponseDate(LocalDateTime.now())
            .respondent1ResponseDate(LocalDateTime.now().minusDays(1))
            .build();

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
        RequestedCourt requestedCourt1 = new RequestedCourt();
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQRequestedCourt(requestedCourt1);
        RequestedCourt requestedCourt2 = new RequestedCourt();
        Respondent2DQ respondent2DQ = new Respondent2DQ();
        respondent2DQ.setRespondent2DQRequestedCourt(requestedCourt2);

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent2ResponseDate(LocalDateTime.now().plusDays(1))
            .build();

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
        RequestedCourt requestedCourt = new RequestedCourt();
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQRequestedCourt(requestedCourt);
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1DQ(respondent1DQ)
            .build();

        SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder = SealedClaimResponseFormForSpec.builder();

        given(featureToggleService.isCarmEnabledForCase(caseData)).willReturn(true);

        statementOfTruthPopulator.populateStatementOfTruthDetails(builder, caseData);

        SealedClaimResponseFormForSpec form = builder.build();
        Assertions.assertTrue(form.isCheckCarmToggle());
        verify(featureToggleService).isCarmEnabledForCase(caseData);
    }
}
