package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.submitsdotests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo.ClaimsTrackBasedOnJudgeSelectionFieldUpdater;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimsTrackBasedOnJudgeSelectionFieldUpdaterTest {

    @InjectMocks
    private ClaimsTrackBasedOnJudgeSelectionFieldUpdater fieldUpdater;

    private CaseData.CaseDataBuilder<?, ?> caseDataBuilder;

    @BeforeEach
    void setUp() {
        caseDataBuilder = CaseData.builder();
    }

    @Test
    void shouldSetAllocatedTrackToSmallClaimForUnspecClaim() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.UNSPEC_CLAIM);

        try (MockedStatic<SdoHelper> mockedSdoHelper = mockStatic(SdoHelper.class)) {
            mockedSdoHelper.when(() -> SdoHelper.isSmallClaimsTrack(caseData)).thenReturn(true);
            mockedSdoHelper.when(() -> SdoHelper.isFastTrack(caseData)).thenReturn(false);

            fieldUpdater.update(caseData, caseDataBuilder);
        }

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getAllocatedTrack()).isEqualTo(AllocatedTrack.SMALL_CLAIM);
    }

    @Test
    void shouldSetAllocatedTrackToFastClaimForUnspecClaim() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.UNSPEC_CLAIM);

        try (MockedStatic<SdoHelper> mockedSdoHelper = mockStatic(SdoHelper.class)) {
            mockedSdoHelper.when(() -> SdoHelper.isFastTrack(caseData)).thenReturn(true);
            mockedSdoHelper.when(() -> SdoHelper.isSmallClaimsTrack(caseData)).thenReturn(false);

            fieldUpdater.update(caseData, caseDataBuilder);
        }

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getAllocatedTrack()).isEqualTo(AllocatedTrack.FAST_CLAIM);
    }

    @Test
    void shouldSetResponseClaimTrackToSmallClaimForSpecClaim() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);

        try (MockedStatic<SdoHelper> mockedSdoHelper = mockStatic(SdoHelper.class)) {
            mockedSdoHelper.when(() -> SdoHelper.isSmallClaimsTrack(caseData)).thenReturn(true);
            mockedSdoHelper.when(() -> SdoHelper.isFastTrack(caseData)).thenReturn(false);

            fieldUpdater.update(caseData, caseDataBuilder);
        }

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getResponseClaimTrack()).isEqualTo(AllocatedTrack.SMALL_CLAIM.name());
    }

    @Test
    void shouldSetResponseClaimTrackToFastClaimForSpecClaim() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);

        try (MockedStatic<SdoHelper> mockedSdoHelper = mockStatic(SdoHelper.class)) {
            mockedSdoHelper.when(() -> SdoHelper.isFastTrack(caseData)).thenReturn(true);
            mockedSdoHelper.when(() -> SdoHelper.isSmallClaimsTrack(caseData)).thenReturn(false);

            fieldUpdater.update(caseData, caseDataBuilder);
        }

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getResponseClaimTrack()).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
    }
}
