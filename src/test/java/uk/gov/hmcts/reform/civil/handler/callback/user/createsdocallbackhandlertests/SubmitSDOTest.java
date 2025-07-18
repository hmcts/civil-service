package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo.SdoCaseDataFieldUpdater;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo.SubmitSDO;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubmitSDOTest {

    @Mock
    private List<SdoCaseDataFieldUpdater> sdoCaseDataFieldUpdaters;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private UpdateWaCourtLocationsService updateWaCourtLocationsService;

    @InjectMocks
    private SubmitSDO submitSDO;

    private CallbackParams callbackParams;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().build();
        callbackParams = CallbackParams.builder().caseData(caseData).build();
        ObjectMapper objectMapper = new ObjectMapper();
        submitSDO = new SubmitSDO(objectMapper, sdoCaseDataFieldUpdaters, featureToggleService, Optional.of(updateWaCourtLocationsService));
    }

    @Test
    void shouldValidateFieldsNihl() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) submitSDO.execute(callbackParams);

        sdoCaseDataFieldUpdaters.forEach(updater -> verify(updater, times(1)).update(caseData, updatedData));
        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
    }
}