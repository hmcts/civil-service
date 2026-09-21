package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.CreateSdoFixtures;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@SuppressWarnings({"java:S5960", "java:S6813"})
class CreateSdoWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private FeatureToggleService featureToggleService;

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
        when(locationRefDataService.getCourtLocationsByEpimmsIdAndCourtType(anyString(), anyString(), anyString()))
            .thenReturn(List.of(locationRefData));
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
    }

    @Test
    void shouldSubmitUnspecFastTrackSdo() throws Exception {
        CaseData fixture = CreateSdoFixtures.unspecFastTrack();

        startWorkflow(fixture)
            .eventId(CREATE_SDO)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(CREATE_SDO.name());
                assertThat(updated.getAllocatedTrack()).isEqualTo(FAST_CLAIM);
                assertThat(updated.getSystemGeneratedCaseDocuments()).isNotEmpty();
            });
    }

    @Test
    void shouldSubmitUnspecSmallClaimsSdo() throws Exception {
        CaseData fixture = CreateSdoFixtures.unspecSmallClaims();

        startWorkflow(fixture)
            .eventId(CREATE_SDO)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(CREATE_SDO.name());
                assertThat(updated.getAllocatedTrack()).isEqualTo(SMALL_CLAIM);
                assertThat(updated.getSystemGeneratedCaseDocuments()).isNotEmpty();
            });
    }

    @Test
    void shouldSubmitSpecSmallClaimsSdoAndSetResponseClaimTrack() throws Exception {
        CaseData fixture = CreateSdoFixtures.specSmallClaims();

        startWorkflow(fixture)
            .eventId(CREATE_SDO)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getResponseClaimTrack()).isEqualTo(SMALL_CLAIM.name());
                assertThat(updated.getSystemGeneratedCaseDocuments()).isNotEmpty();
            });
    }
}
