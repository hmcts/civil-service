package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingDates;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.*;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    HearingScheduledHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
})
public class HearingScheduledHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private HearingScheduledHandler handler;
    @MockBean
    private LocationRefDataService locationRefDataService;

//        .put(callbackKey(MID, "locationName"), this::locationList) DONE
//        .put(callbackKey(MID, "checkPastDate"), this::checkPastDate) & not left blank
//        .put(callbackKey(MID, "checkFutureDate"), this::checkFutureDate) & not left blank
//        .put(callbackKey(SUBMITTED), this::buildConfirmation)
// Check that when "What hearing notice do you want to create? and "Other" is selected, the textField is not empty

    @Nested
    class MidEventCheckLocationListCallback {

        private static final String PAGE_ID = "locationName";

        @Test
        void shouldReturnLocationList_whenLocationsAreQueried() {
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().courtName("Court Name").region("Region").build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("hearingLocation")).isNotNull();
        }
    }

    @Nested
    class MidEventCheckPastDateCallback {

        private static final String PAGE_ID = "checkPastDate";

        @Test
        void shouldReturnError_whenDateFromDateGreaterThanPresentDateProvided() {
            HearingDates hearingDates = HearingDates.builder().hearingUnavailableFrom(
                LocalDate.now().plusMonths(2)).hearingUnavailableUntil(
                LocalDate.now().plusMonths(1)).build();
            HearingSupportRequirementsDJ hearingSupportRequirementsDJ = HearingSupportRequirementsDJ.builder()
                .hearingDates(
                    wrapElements(hearingDates)).build();

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .hearingSupportRequirementsDJ(hearingSupportRequirementsDJ)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .hearingSupportRequirementsDJ(hearingSupportRequirementsDJ)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("Unavailable From Date should be less than To Date");
        }

    }

    @Nested
    class MidEventCheckFutureDateCallback {

        private static final String PAGE_ID = "checkFutureDate";
        @Test
        void shouldReturnError_whenDateFromDateEarlierThanPresentDateProvided() {
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnHearingNoticeCreated() {

        }
    }
}


