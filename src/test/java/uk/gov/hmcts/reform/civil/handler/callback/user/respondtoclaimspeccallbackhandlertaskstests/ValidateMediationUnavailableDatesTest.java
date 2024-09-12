package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class ValidateMediationUnavailableDatesTest {

    @InjectMocks
    private ValidateMediationUnavailableDates validateMediationUnavailableDates;

    @Mock
    private CallbackParams callbackParams;

    @BeforeEach
    void setUp() {
        when(callbackParams.getCaseData()).thenReturn(CaseData.builder().build());
    }

    @Test
    void shouldReturnErrorsWhenResp1MediationAvailabilityExists() {
        MediationAvailability mediationAvailability = MediationAvailability.builder()
            .isMediationUnavailablityExists(YES)
            .unavailableDatesForMediation(wrapElements(UnavailableDate.builder()
                                                           .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                           .date(LocalDate.now().minusYears(5))
                                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .resp1MediationAvailability(mediationAvailability)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateMediationUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).isNotEmpty();
    }

    @Test
    void shouldReturnNoErrorsWhenResp1MediationAvailabilityDoesNotExist() {
        MediationAvailability mediationAvailability = MediationAvailability.builder()
            .isMediationUnavailablityExists(NO)
            .unavailableDatesForMediation(wrapElements(UnavailableDate.builder()
                                                           .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                           .date(LocalDate.now().minusYears(5))
                                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .resp1MediationAvailability(mediationAvailability)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateMediationUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenResp2MediationAvailabilityExists() {
        MediationAvailability mediationAvailability = MediationAvailability.builder()
            .isMediationUnavailablityExists(YES)
            .unavailableDatesForMediation(wrapElements(UnavailableDate.builder()
                                                           .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                           .date(LocalDate.now().minusYears(5))
                                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .resp2MediationAvailability(mediationAvailability)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateMediationUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).isNotEmpty();
    }

    @Test
    void shouldReturnNoErrorsWhenResp2MediationAvailabilityDoesNotExist() {
        MediationAvailability mediationAvailability = MediationAvailability.builder()
            .isMediationUnavailablityExists(NO)
            .unavailableDatesForMediation(wrapElements(UnavailableDate.builder()
                                                           .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                           .date(LocalDate.now().minusYears(5))
                                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .resp2MediationAvailability(mediationAvailability)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateMediationUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoErrorsWhenNoMediationAvailabilityExists() {
        CaseData caseData = CaseData.builder().build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateMediationUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }
}
