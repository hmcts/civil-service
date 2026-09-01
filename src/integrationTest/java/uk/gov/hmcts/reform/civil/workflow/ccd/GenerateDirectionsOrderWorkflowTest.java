package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.GenerateDirectionsOrderFixtures;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_ORDER_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;

@SuppressWarnings({"java:S5960", "java:S6813"})
class GenerateDirectionsOrderWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private UpdateWaCourtLocationsService updateWaCourtLocationsService;

    @BeforeEach
    void setUp() {
        LocationRefData locationRefData = new LocationRefData()
            .setEpimmsId("123456")
            .setSiteName("Test Court")
            .setCourtAddress("1 Court Road")
            .setPostcode("AB1 2CD")
            .setRegionId("1");
        when(locationRefDataService.getHearingCourtLocations(anyString(), anyString()))
            .thenReturn(List.of(locationRefData));
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

        when(userService.getUserDetails(anyString()))
            .thenReturn(UserDetails.builder()
                            .forename("Judge")
                            .surname("Test")
                            .email("judge@example.com")
                            .build());
    }

    @Test
    void shouldSetStateToCaseProgressionWhenIntermediateFromJudicialReferral() throws Exception {
        CaseData fixture = GenerateDirectionsOrderFixtures.intermediateFromJudicialReferral();

        startWorkflow(fixture)
            .eventId(GENERATE_DIRECTIONS_ORDER)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CASE_PROGRESSION.name());

                CaseData updated = result.caseData();
                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(GENERATE_ORDER_NOTIFICATION.name());
                assertThat(updated.getFinalOrderDocument()).isNull();
            });
    }

    @Test
    void shouldNotForceStateToCaseProgressionOnFreeFormFromCaseProgression() throws Exception {
        CaseData fixture = GenerateDirectionsOrderFixtures.freeFormFromCaseProgression();

        startWorkflow(fixture)
            .eventId(GENERATE_DIRECTIONS_ORDER)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isNull();

                CaseData updated = result.caseData();
                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(GENERATE_ORDER_NOTIFICATION.name());
            });
    }
}
