package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.CvpJoinLinkEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.model.hearing.Attendees;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearings.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_CVP_JOIN_LINK;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.INTER;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.VIDCVP;

@ExtendWith(SpringExtension.class)
class TriggerHearingCvpLinkEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private HearingsService hearingService;

    @Mock
    private UserService userService;

    @Mock
    private SystemUpdateUserConfiguration userConfig;

    @InjectMocks
    private TriggerHearingCvpLinkEventHandler handler;

    @BeforeEach
    private void setup() {
        when(userService.getAccessToken(anyString(), anyString())).thenReturn("auth");
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("test-id").build());
        when(userConfig.getUserName()).thenReturn("");
        when(userConfig.getPassword()).thenReturn("");
    }

    @Test
    void shouldCallTriggerEventWithExpectedParams_whenThereIsOneCaseHearingWithVideoHearing() {
        CvpJoinLinkEvent event = new CvpJoinLinkEvent(1L);

        when(hearingService.getHearings(anyString(), anyLong(), anyString())).thenReturn(
            HearingsResponse.builder()
                .caseRef("reference")
                .hmctsServiceCode("AAA7")
                .caseHearings(List.of(CaseHearing.builder()
                                          .hearingDaySchedule(List.of(
                                              HearingDaySchedule.builder()
                                                  .attendees(List.of(
                                                      Attendees.builder()
                                                          .hearingSubChannel(VIDCVP)
                                                          .build(),
                                                      Attendees.builder()
                                                          .hearingSubChannel(null)
                                                          .build()
                                                  )).build()
                                          ))
                                          .build()))
                .build());

        handler.triggerCvpJoinLinkEvent(event);

        verify(coreCaseDataService, times(1)).triggerEvent(event.getCaseId(), SEND_CVP_JOIN_LINK);
    }

    @Test
    void shouldCallTriggerEventWithExpectedParams_whenThereAreMultipleCaseHearings() {
        CvpJoinLinkEvent event = new CvpJoinLinkEvent(1L);

        when(hearingService.getHearings(anyString(), anyLong(), anyString())).thenReturn(
            HearingsResponse.builder()
                .caseRef("reference")
                .hmctsServiceCode("AAA7")
                .caseHearings(List.of(
                    CaseHearing.builder()
                        .hearingDaySchedule(List.of(
                            HearingDaySchedule.builder()
                                .attendees(List.of(
                                    Attendees.builder()
                                        .hearingSubChannel(INTER)
                                        .build()
                                )).build()
                        ))
                        .build(),
                    CaseHearing.builder()
                        .hearingDaySchedule(List.of(
                            HearingDaySchedule.builder()
                                .attendees(List.of(
                                    Attendees.builder()
                                        .hearingSubChannel(VIDCVP)
                                        .build()
                                )).build()
                        ))
                        .build()))
                .build());

        handler.triggerCvpJoinLinkEvent(event);

        verify(coreCaseDataService, times(1)).triggerEvent(event.getCaseId(), SEND_CVP_JOIN_LINK);
    }

    @Test
    void shouldNotCallTriggerEventWithExpectedParams_whenCaseHearingsDoNotContainAVideoHearing() {
        CvpJoinLinkEvent event = new CvpJoinLinkEvent(1L);

        when(hearingService.getHearings(anyString(), anyLong(), anyString())).thenReturn(
            HearingsResponse.builder()
                .caseRef("reference")
                .hmctsServiceCode("AAA7")
                .caseHearings(List.of(
                    CaseHearing.builder()
                        .hearingDaySchedule(List.of(
                            HearingDaySchedule.builder()
                                .attendees(List.of(
                                    Attendees.builder()
                                        .hearingSubChannel(INTER)
                                        .build()
                                )).build()
                        ))
                        .build()))
                .build());

        handler.triggerCvpJoinLinkEvent(event);

        verify(coreCaseDataService, times(0)).triggerEvent(event.getCaseId(), SEND_CVP_JOIN_LINK);
    }

    @Test
    void shouldNotCallTriggerEventWithExpectedParams_whenThereCaseHearingsIsNull() {
        CvpJoinLinkEvent event = new CvpJoinLinkEvent(1L);

        when(hearingService.getHearings(anyString(), anyLong(), anyString())).thenReturn(
            HearingsResponse.builder()
                .caseRef("reference")
                .hmctsServiceCode("AAA7")
                .build());

        handler.triggerCvpJoinLinkEvent(event);

        verify(coreCaseDataService, times(0)).triggerEvent(event.getCaseId(), SEND_CVP_JOIN_LINK);
    }

    @Test
    void shouldNotCallTriggerEventWithExpectedParams_whenThereAreNoCaseHearings() {
        CvpJoinLinkEvent event = new CvpJoinLinkEvent(1L);

        when(hearingService.getHearings(anyString(), anyLong(), anyString())).thenReturn(
            HearingsResponse.builder()
                .caseRef("reference")
                .hmctsServiceCode("AAA7")
                .caseHearings(List.of())
                .build());

        handler.triggerCvpJoinLinkEvent(event);

        verify(coreCaseDataService, times(0)).triggerEvent(event.getCaseId(), SEND_CVP_JOIN_LINK);
    }

}
