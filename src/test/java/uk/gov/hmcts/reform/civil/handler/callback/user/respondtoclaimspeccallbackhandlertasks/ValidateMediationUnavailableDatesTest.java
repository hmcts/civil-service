package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ValidateMediationUnavailableDatesTest {

    private ValidateMediationUnavailableDates validateMediationUnavailableDates;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        validateMediationUnavailableDates = new ValidateMediationUnavailableDates(mapper);
    }

    private static MediationAvailability buildMediationAvailability(YesOrNo isUnavailable, LocalDate date) {
        return MediationAvailability.builder()
            .isMediationUnavailablityExists(isUnavailable)
            .unavailableDatesForMediation(wrapElements(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(date)
                    .build()
            ))
            .build();
    }

    @Test
    void shouldReturnErrorsWhenResp1MediationAvailabilityExists() {
        MediationAvailability mediationAvailability = buildMediationAvailability(YES, LocalDate.now().minusYears(1));
        CaseData caseData = CaseData.builder().resp1MediationAvailability(mediationAvailability).build();
        CallbackParams params = CallbackParamsBuilder.builder().of(null, caseData).build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateMediationUnavailableDates.execute(params);

        assertThat(response.getErrors()).isNotEmpty();
    }

    @Test
    void shouldReturnNoErrorsWhenResp1MediationAvailabilityDoesNotExist() {
        MediationAvailability mediationAvailability = buildMediationAvailability(NO, LocalDate.now().plusDays(2));
        CaseData caseData = CaseData.builder().resp1MediationAvailability(mediationAvailability).build();
        CallbackParams params = CallbackParamsBuilder.builder().of(null, caseData).build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateMediationUnavailableDates.execute(params);

        assertThat(response.getErrors()).isNull();
        Map<String, Object> responseData = response.getData();
        assertThat(responseData).isNotNull();
    }

    @Test
    void shouldReturnErrorsWhenResp2MediationAvailabilityExists() {
        MediationAvailability mediationAvailability = buildMediationAvailability(YES, LocalDate.now().minusDays(3));
        CaseData caseData = CaseData.builder().resp2MediationAvailability(mediationAvailability).build();
        CallbackParams params = CallbackParamsBuilder.builder().of(null, caseData).build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateMediationUnavailableDates.execute(params);

        assertThat(response.getErrors()).isNotEmpty();
    }

    @Test
    void shouldReturnNoErrorsWhenResp2MediationAvailabilityDoesNotExist() {
        MediationAvailability mediationAvailability = buildMediationAvailability(NO, LocalDate.now().plusDays(5));
        CaseData caseData = CaseData.builder().resp2MediationAvailability(mediationAvailability).build();
        CallbackParams params = CallbackParamsBuilder.builder().of(null, caseData).build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateMediationUnavailableDates.execute(params);

        assertThat(response.getErrors()).isEmpty();
        Map<String, Object> responseData = response.getData();
        assertThat(responseData).isNotNull();
    }

    @Test
    void shouldReturnNoErrorsWhenNoMediationAvailabilityExists() {
        CaseData caseData = CaseData.builder().build();
        CallbackParams params = CallbackParamsBuilder.builder().of(null, caseData).build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateMediationUnavailableDates.execute(params);

        assertThat(response.getErrors()).isEmpty();
        Map<String, Object> responseData = response.getData();
        assertThat(responseData).isNotNull();
    }

    @Test
    void shouldNormalizeValidFutureDate() {
        LocalDate validDate = LocalDate.now().plusDays(2);
        MediationAvailability mediationAvailability = buildMediationAvailability(YES, validDate);
        CaseData caseData = CaseData.builder().resp1MediationAvailability(mediationAvailability).build();
        CallbackParams params = CallbackParamsBuilder.builder().of(null, caseData).build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateMediationUnavailableDates.execute(params);

        assertThat(response.getErrors()).isEmpty();

        CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
        UnavailableDate dateEntry =
            updatedCaseData.getResp1MediationAvailability().getUnavailableDatesForMediation().get(0).getValue();
        assertThat(dateEntry.getFromDate()).isNull();
        assertThat(dateEntry.getToDate()).isNull();
    }
}
