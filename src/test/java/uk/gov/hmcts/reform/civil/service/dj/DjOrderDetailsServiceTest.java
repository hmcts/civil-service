package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@ExtendWith(MockitoExtension.class)
class DjOrderDetailsServiceTest {

    private static final String TOKEN = "auth-token";
    private static final String JUDGE = "Recorder Blue";

    @Mock
    private DjLocationAndToggleService locationAndToggleService;
    @Mock
    private DjDisposalDirectionsService disposalDirectionsService;
    @Mock
    private DjTrialDirectionsService trialDirectionsService;
    @Mock
    private UserService userService;
    @Mock
    private UserDetails userDetails;

    private DjOrderDetailsService service;

    @BeforeEach
    void setUp() {
        service = new DjOrderDetailsService(
            locationAndToggleService,
            disposalDirectionsService,
            trialDirectionsService,
            userService
        );
    }

    @Test
    void shouldPopulateDisposalAndTrialDirections() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .legacyCaseReference("MC001")
            .build();
        DirectionsOrderTaskContext context = new DirectionsOrderTaskContext(
            caseData,
            new CallbackParams()
                .params(Map.of(BEARER_TOKEN, TOKEN)),
            DirectionsOrderLifecycleStage.ORDER_DETAILS
        );
        when(locationAndToggleService.prepareLocationsAndToggles(context)).thenReturn(caseData);
        when(userService.getUserDetails(TOKEN)).thenReturn(userDetails);
        when(userDetails.getFullName()).thenReturn(JUDGE);

        service.populateTrialDisposalScreen(context);

        ArgumentCaptor<CaseData.CaseDataBuilder> builderCaptor = ArgumentCaptor.forClass(CaseData.CaseDataBuilder.class);
        verify(disposalDirectionsService).populateDisposalDirections(builderCaptor.capture(), org.mockito.Mockito.eq(JUDGE));
        verify(trialDirectionsService).populateTrialDirections(builderCaptor.capture(), org.mockito.Mockito.eq(JUDGE));
        assertThat(builderCaptor.getAllValues()).hasSize(2);
    }

    @Test
    void shouldApplyHearingSelectionsViaLocationService() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CallbackParams params = new CallbackParams().version(null);

        service.applyHearingSelections(caseData, params.getVersion());

        verify(locationAndToggleService).applyHearingSelections(caseData, params.getVersion());
    }
}
