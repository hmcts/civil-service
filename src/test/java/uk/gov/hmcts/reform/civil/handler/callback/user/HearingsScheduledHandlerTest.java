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
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.repositories.HearingReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.bankholidays.PublicHolidaysCollection;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    HearingScheduledHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
})
public class HearingsScheduledHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private HearingScheduledHandler handler;
    @MockBean
    private PublicHolidaysCollection publicHolidaysCollection;
    @MockBean
    private HearingReferenceNumberRepository hearingReferenceNumberRepository;
    @MockBean
    private  LocationRefDataService locationRefDataService;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldGetDueDateAndFeeSmallClaim_whenAboutToSubmit() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(NO)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .hearingReferenceNumber("00HNC001")
                .hearingDateTime(LocalDateTime.now().plusWeeks(2))
                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                          .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            Set<LocalDate> publicHolidays = new HashSet<>();
            publicHolidays.add(LocalDate.now().plusDays(3));
            when(publicHolidaysCollection.getPublicHolidays()).thenReturn(publicHolidays);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHearingFee()).isEqualTo("£545");
        }

        @Test
        void shouldGetDueDateAndFeeMultiClaim_whenAboutToSubmit() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(NO)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .hearingReferenceNumber("00HNC001")
                .hearingDateTime(LocalDateTime.now().plusWeeks(5))
                .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            Set<LocalDate> publicHolidays = new HashSet<>();
            publicHolidays.add(LocalDate.now().plusDays(3));
            when(publicHolidaysCollection.getPublicHolidays()).thenReturn(publicHolidays);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHearingFee()).isEqualTo("£1.175");
        }

        @Test
        void shouldGetDueDateAndFeeFastClaim_whenAboutToSubmit() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(NO)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .hearingReferenceNumber("00HNC001")
                .hearingDateTime(LocalDateTime.now().plusWeeks(5))
                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            Set<LocalDate> publicHolidays = new HashSet<>();
            publicHolidays.add(LocalDate.now().plusDays(3));
            when(publicHolidaysCollection.getPublicHolidays()).thenReturn(publicHolidays);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHearingFee()).isEqualTo("£27");
        }
    }
}


