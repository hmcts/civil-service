package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class HearingDocumentDeadlineParamsBuilderTest {

    private HearingDocumentDeadlineParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new HearingDocumentDeadlineParamsBuilder();
    }

    @Test
    void shouldReturnEmptyForSmallClaimsTrack() {
        CaseData caseData = mock(CaseData.class);
        try (MockedStatic<SdoHelper> mockedStatic = mockStatic(SdoHelper.class)) {
            mockedStatic.when(() -> SdoHelper.isSmallClaimsTrack(caseData)).thenReturn(true);

            Optional<LocalDate> result = builder.getHearingDocumentDeadline(caseData);

            assertThat(result).isEmpty();
        }
    }

    @Test
    void shouldReturnFastTrackDisclosureDate() {
        CaseData caseData = mock(CaseData.class);
        FastTrackDisclosureOfDocuments fastTrackDisclosure = mock(FastTrackDisclosureOfDocuments.class);
        LocalDate expectedDate = LocalDate.of(2023, 10, 20);

        try (MockedStatic<SdoHelper> mockedStatic = mockStatic(SdoHelper.class)) {
            mockedStatic.when(() -> SdoHelper.isSmallClaimsTrack(caseData)).thenReturn(false);
            mockedStatic.when(() -> SdoHelper.isFastTrack(caseData)).thenReturn(true);
            when(caseData.getFastTrackDisclosureOfDocuments()).thenReturn(fastTrackDisclosure);
            when(fastTrackDisclosure.getDate3()).thenReturn(expectedDate);

            Optional<LocalDate> result = builder.getHearingDocumentDeadline(caseData);

            assertThat(result).contains(expectedDate);
        }
    }

    @Test
    void shouldReturnDisposalHearingDisclosureDate() {
        CaseData caseData = mock(CaseData.class);
        DisposalHearingDisclosureOfDocuments disposalDisclosure = mock(DisposalHearingDisclosureOfDocuments.class);
        LocalDate expectedDate = LocalDate.of(2023, 10, 25);

        try (MockedStatic<SdoHelper> mockedStatic = mockStatic(SdoHelper.class)) {
            mockedStatic.when(() -> SdoHelper.isSmallClaimsTrack(caseData)).thenReturn(false);
            mockedStatic.when(() -> SdoHelper.isFastTrack(caseData)).thenReturn(false);
            when(caseData.getDisposalHearingDisclosureOfDocuments()).thenReturn(disposalDisclosure);
            when(disposalDisclosure.getDate2()).thenReturn(expectedDate);

            Optional<LocalDate> result = builder.getHearingDocumentDeadline(caseData);

            assertThat(result).contains(expectedDate);
        }
    }

    @Test
    void shouldReturnEmptyWhenNoDisclosureDatesArePresent() {
        CaseData caseData = mock(CaseData.class);

        try (MockedStatic<SdoHelper> mockedStatic = mockStatic(SdoHelper.class)) {
            mockedStatic.when(() -> SdoHelper.isSmallClaimsTrack(caseData)).thenReturn(false);
            mockedStatic.when(() -> SdoHelper.isFastTrack(caseData)).thenReturn(false);

            when(caseData.getDisposalHearingDisclosureOfDocuments()).thenReturn(null);

            Optional<LocalDate> result = builder.getHearingDocumentDeadline(caseData);

            assertThat(result).isEmpty();
        }
    }
}
