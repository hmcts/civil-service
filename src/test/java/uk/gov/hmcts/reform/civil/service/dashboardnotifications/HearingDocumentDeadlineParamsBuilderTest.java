package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HearingDocumentDeadlineParamsBuilderTest {

    private HearingDocumentDeadlineParamsBuilder builder;
    private SdoHelper sdoHelper;

    @BeforeEach
    void setUp() {
        sdoHelper = mock(SdoHelper.class);
        builder = new HearingDocumentDeadlineParamsBuilder(sdoHelper);
    }

    @Test
    void shouldReturnEmptyForSmallClaimsTrack() {
        CaseData caseData = mock(CaseData.class);
        when(sdoHelper.isSmallClaimsTrack(caseData)).thenReturn(true);

        Optional<LocalDate> result = builder.getHearingDocumentDeadline(caseData);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnFastTrackDisclosureDate() {
        CaseData caseData = mock(CaseData.class);
        FastTrackDisclosureOfDocuments fastTrackDisclosure = mock(FastTrackDisclosureOfDocuments.class);
        LocalDate expectedDate = LocalDate.of(2023, 10, 20);

        when(sdoHelper.isSmallClaimsTrack(caseData)).thenReturn(false);
        when(sdoHelper.isFastTrack(caseData)).thenReturn(true);
        when(caseData.getFastTrackDisclosureOfDocuments()).thenReturn(fastTrackDisclosure);
        when(fastTrackDisclosure.getDate3()).thenReturn(expectedDate);

        Optional<LocalDate> result = builder.getHearingDocumentDeadline(caseData);

        assertThat(result).contains(expectedDate);
    }

    @Test
    void shouldReturnDisposalHearingDisclosureDate() {
        CaseData caseData = mock(CaseData.class);
        DisposalHearingDisclosureOfDocuments disposalDisclosure = mock(DisposalHearingDisclosureOfDocuments.class);
        LocalDate expectedDate = LocalDate.of(2023, 10, 25);

        when(sdoHelper.isSmallClaimsTrack(caseData)).thenReturn(false);
        when(sdoHelper.isFastTrack(caseData)).thenReturn(false);
        when(caseData.getDisposalHearingDisclosureOfDocuments()).thenReturn(disposalDisclosure);
        when(disposalDisclosure.getDate2()).thenReturn(expectedDate);

        Optional<LocalDate> result = builder.getHearingDocumentDeadline(caseData);

        assertThat(result).contains(expectedDate);
    }

    @Test
    void shouldReturnEmptyWhenNoDisclosureDatesArePresent() {
        CaseData caseData = mock(CaseData.class);

        when(sdoHelper.isSmallClaimsTrack(caseData)).thenReturn(false);
        when(sdoHelper.isFastTrack(caseData)).thenReturn(false);
        when(caseData.getDisposalHearingDisclosureOfDocuments()).thenReturn(null);

        Optional<LocalDate> result = builder.getHearingDocumentDeadline(caseData);

        assertThat(result).isEmpty();
    }
}
