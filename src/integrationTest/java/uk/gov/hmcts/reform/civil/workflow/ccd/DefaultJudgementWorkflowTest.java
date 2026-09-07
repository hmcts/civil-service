package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.DefaultJudgementFixtures;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SuppressWarnings({"java:S5960", "java:S6813"})
class DefaultJudgementWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean(name = "deadlinesCalculator")
    private DeadlinesCalculator deadlinesCalculator;

    @BeforeEach
    void setUp() {
        when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(anyInt(), any()))
            .thenReturn(LocalDateTime.now().plusMonths(36));

        LocationRefData locationRefData = new LocationRefData()
            .setEpimmsId("456789")
            .setSiteName("Court Name")
            .setCourtAddress("Address")
            .setPostcode("Postcode")
            .setRegionId("1");
        when(locationRefDataService.getCourtLocationsForDefaultJudgments(anyString(), anyString()))
            .thenReturn(List.of(locationRefData));
        when(locationRefDataService.getCourtLocationsByEpimmsIdAndCourtType(anyString(), anyString(), anyString()))
            .thenReturn(List.of(locationRefData));
    }

    @Test
    void shouldSubmitUnspecDefaultJudgment1v1() throws Exception {
        CaseData fixture = DefaultJudgementFixtures.unspecDj1v1();

        startWorkflow(fixture)
            .eventId(DEFAULT_JUDGEMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(DEFAULT_JUDGEMENT.name());
                assertThat(updated.getSetRequestDJDamagesFlagForWA()).isEqualTo(YES);
                assertThat(updated.getClaimDismissedDeadline()).isNotNull();
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse()).isNotNull());
    }

    @Test
    void shouldReturnErrorOnAboutToStartWhenDeadlineNotPassed() throws Exception {
        CaseData fixture = DefaultJudgementFixtures.unspecDjDeadlineNotPassed();

        startWorkflow(fixture)
            .eventId(DEFAULT_JUDGEMENT)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNotEmpty();
                assertThat(result.response().getErrors().get(0))
                    .contains("The Claim is not eligible for Default Judgment until");
            });
    }

    @Test
    void shouldSetReferToJudgeWhenDivergent1v2SingleDefendant() throws Exception {
        CaseData fixture = DefaultJudgementFixtures.unspecDj1v2SingleDefendant();

        startWorkflow(fixture)
            .eventId(DEFAULT_JUDGEMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(DEFAULT_JUDGEMENT.name());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse()).isNotNull();
            });
    }
}
