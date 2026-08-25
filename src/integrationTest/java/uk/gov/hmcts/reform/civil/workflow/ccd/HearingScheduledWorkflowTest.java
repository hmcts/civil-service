package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.HearingScheduledFixtures;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_PAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_UNPAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;

@SuppressWarnings({"java:S5960", "java:S6813"})
class HearingScheduledWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private HearingFeesService hearingFeesService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private Time time;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 10, 0);

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(NOW);
        LocationRefData locationRefData = new LocationRefData()
            .setEpimmsId("123456")
            .setSiteName("Test Court")
            .setCourtAddress("1 Court Road")
            .setPostcode("AB1 2CD")
            .setRegionId("1");
        when(locationRefDataService.getHearingCourtLocations(anyString(), anyString()))
            .thenReturn(List.of(locationRefData));
        when(hearingFeesService.getFeeForHearingFastTrackClaims(any(BigDecimal.class)))
            .thenReturn(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(54500)).setCode("FEE0225"));
        when(hearingFeesService.getFeeForHearingSmallClaims(any(BigDecimal.class)))
            .thenReturn(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(27500)).setCode("FEE0221"));
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);
    }

    @Test
    void shouldTransitionToHearingReadinessOnFastTrackListing() throws Exception {
        CaseData fixture = HearingScheduledFixtures.fastTrackListing();

        startWorkflow(fixture)
            .eventId(HEARING_SCHEDULED)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(HEARING_READINESS.name());

                CaseData updated = result.caseData();
                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(HEARING_SCHEDULED.name());
                assertThat(updated.getHearingReferenceNumber()).isNotNull();
                assertThat(updated.getHearingDueDate()).isNotNull();
            });
    }

    @Test
    void shouldTransitionToPrepareForHearingOnRelisting() throws Exception {
        CaseData fixture = HearingScheduledFixtures.fastTrackRelisting();

        startWorkflow(fixture)
            .eventId(HEARING_SCHEDULED)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState())
                    .isEqualTo(PREPARE_FOR_HEARING_CONDUCT_HEARING.name());

                CaseData updated = result.caseData();
                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
            });
    }

    @Test
    void shouldTransitionToPrepareForHearingOnOtherHearingType() throws Exception {
        CaseData fixture = HearingScheduledFixtures.otherHearingType();

        startWorkflow(fixture)
            .eventId(HEARING_SCHEDULED)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState())
                    .isEqualTo(PREPARE_FOR_HEARING_CONDUCT_HEARING.name());
            });
    }

    @Test
    void shouldTransitionToPrepareForHearingOnHearingFeePaid() throws Exception {
        CaseData fixture = HearingScheduledFixtures.fastTrackListing().toBuilder()
            .ccdState(HEARING_READINESS)
            .build();

        startWorkflow(fixture)
            .eventId(HEARING_FEE_PAID)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState())
                    .isEqualTo(PREPARE_FOR_HEARING_CONDUCT_HEARING.name());
            });
    }

    @Test
    void shouldDismissCaseOnHearingFeeUnpaid() throws Exception {
        CaseData fixture = HearingScheduledFixtures.fastTrackListing().toBuilder()
            .ccdState(HEARING_READINESS)
            .build();

        startWorkflow(fixture)
            .eventId(HEARING_FEE_UNPAID)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CASE_DISMISSED.name());

                CaseData updated = result.caseData();
                assertThat(updated.getCaseDismissedHearingFeeDueDate()).isNotNull();
                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(HEARING_FEE_UNPAID.name());
            });
    }
}
