package uk.gov.hmcts.reform.civil.service.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnnotifiedHearingsSearchServiceTest {

    private static final String USERNAME = "system-user";
    private static final String PASSWORD = "password";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String SPEC_SERVICE_ID = "AAA6";
    private static final String UNSPEC_SERVICE_ID = "AAA7";

    @Mock
    private UserService userService;
    @Mock
    private HearingsService hearingsService;

    private UnnotifiedHearingsSearchService service;

    @BeforeEach
    void setUp() {
        service = new UnnotifiedHearingsSearchService(
            userService,
            new SystemUpdateUserConfiguration(USERNAME, PASSWORD),
            hearingsService
        );
        ReflectionTestUtils.setField(service, "serviceIds", List.of(SPEC_SERVICE_ID, UNSPEC_SERVICE_ID));
    }

    @Test
    void shouldReturnUnnotifiedHearingsAcrossConfiguredServices() {
        when(userService.getAccessToken(USERNAME, PASSWORD)).thenReturn(ACCESS_TOKEN);
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(SPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of("hearing-1"), 1L));
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(UNSPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of("hearing-2", "hearing-3"), 2L));

        TaskResult<String> result = service.getUnnotifiedHearings();

        assertThat(result.totalResults()).isEqualTo(3);
        assertThat(result.itemStream()).containsExactly("hearing-1", "hearing-2", "hearing-3");
        assertThat(result.isEmpty()).isFalse();
        verify(hearingsService).getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(SPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        );
        verify(hearingsService).getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(UNSPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        );
    }

    @Test
    void shouldHandleNullHearingIds() {
        when(userService.getAccessToken(USERNAME, PASSWORD)).thenReturn(ACCESS_TOKEN);
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(SPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(null, 0L));
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(UNSPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of(), 0L));

        TaskResult<String> result = service.getUnnotifiedHearings();

        assertThat(result.totalResults()).isZero();
        assertThat(result.itemStream()).isEmpty();
        assertThat(result.isEmpty()).isTrue();
    }
}
