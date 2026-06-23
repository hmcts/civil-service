package uk.gov.hmcts.reform.civil.scheduler.bundlecreation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.NoCacheUserService;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BundleCreationScheduledTaskTest {

    private static final long CASE_ID = 123L;
    private static final String USERNAME = "system-user";
    private static final String PASSWORD = "password";
    private static final String ACCESS_TOKEN = "access-token";
    private static final LocalDate HEARING_DATE = LocalDate.of(2026, Month.JULY, 1);

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private NoCacheUserService noCacheUserService;

    private BundleCreationScheduledTask task;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        task = new BundleCreationScheduledTask(
            applicationEventPublisher,
            caseDetailsConverter,
            coreCaseDataService,
            new SystemUpdateUserConfiguration(USERNAME, PASSWORD),
            noCacheUserService,
            0
        );
        caseDetails = CaseDetails.builder().id(CASE_ID).data(Map.of()).build();
    }

    @Test
    void shouldPublishBundleCreationEventWhenBundleDoesNotExist() {
        CaseData caseData = new CaseDataBuilder()
            .hearingDate(HEARING_DATE)
            .caseBundles(List.of())
            .build();
        mockCaseData(caseData);
        when(noCacheUserService.getAccessToken(USERNAME, PASSWORD)).thenReturn(ACCESS_TOKEN);

        task.accept(caseDetails);

        verify(applicationEventPublisher).publishEvent(new BundleCreationTriggerEvent(CASE_ID, ACCESS_TOKEN));
    }

    @Test
    void shouldNotPublishEventWhenBundleAlreadyExistsForHearingDate() {
        Bundle bundle = new Bundle().setBundleHearingDate(Optional.of(HEARING_DATE));
        CaseData caseData = new CaseDataBuilder()
            .hearingDate(HEARING_DATE)
            .caseBundles(List.of(new IdValue<>("bundle-id", bundle)))
            .build();
        mockCaseData(caseData);

        task.accept(caseDetails);

        verifyNoInteractions(applicationEventPublisher, noCacheUserService);
    }

    @Test
    void shouldPublishEventWhenBundlesAreNull() {
        CaseData caseData = new CaseDataBuilder()
            .hearingDate(HEARING_DATE)
            .caseBundles(null)
            .build();
        mockCaseData(caseData);
        when(noCacheUserService.getAccessToken(USERNAME, PASSWORD)).thenReturn(ACCESS_TOKEN);

        task.accept(caseDetails);

        verify(applicationEventPublisher).publishEvent(new BundleCreationTriggerEvent(CASE_ID, ACCESS_TOKEN));
    }

    @Test
    void shouldTreatMissingHearingDateAsNotCreated() {
        CaseData caseData = new CaseDataBuilder()
            .hearingDate(null)
            .caseBundles(List.of())
            .build();
        mockCaseData(caseData);

        assertThat(task.isBundleCreatedForHearingDate(CASE_ID)).isFalse();
    }

    @Test
    void shouldIgnoreNullBundleEntry() {
        CaseData caseData = new CaseDataBuilder()
            .hearingDate(HEARING_DATE)
            .caseBundles(Collections.singletonList(null))
            .build();
        mockCaseData(caseData);

        assertThat(task.isBundleCreatedForHearingDate(CASE_ID)).isFalse();
        verify(noCacheUserService, never()).getAccessToken(USERNAME, PASSWORD);
    }

    @Test
    void shouldRestoreInterruptedFlagWhenThrottleIsInterrupted() {
        task = new BundleCreationScheduledTask(
            applicationEventPublisher,
            caseDetailsConverter,
            coreCaseDataService,
            new SystemUpdateUserConfiguration(USERNAME, PASSWORD),
            noCacheUserService,
            1000
        );
        CaseData caseData = new CaseDataBuilder()
            .hearingDate(HEARING_DATE)
            .caseBundles(List.of())
            .build();
        mockCaseData(caseData);
        when(noCacheUserService.getAccessToken(USERNAME, PASSWORD)).thenReturn(ACCESS_TOKEN);

        try {
            Thread.currentThread().interrupt();

            task.accept(caseDetails);

            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    private void mockCaseData(CaseData caseData) {
        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);
    }
}
