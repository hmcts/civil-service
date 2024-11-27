package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.submitsdotests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo.SdoR2SmallClaimsHearingFieldUpdater;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo.SubmitSdoUtils;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseData.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoR2SmallClaimsHearingFieldUpdaterTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private SubmitSdoUtils submitSdoUtils;

    @InjectMocks
    private SdoR2SmallClaimsHearingFieldUpdater sdoR2SmallClaimsHearingFieldUpdater;

    @Test
    void shouldUpdateHearingFieldsWhenFeatureToggleIsEnabled() {
        DynamicList options = DynamicList.builder()
                .listItems(List.of(
                        DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                        DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                        DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                ))
                .build();

        SdoR2SmallClaimsHearing hearing = SdoR2SmallClaimsHearing.builder()
                .hearingCourtLocationList(options)
                .altHearingCourtLocationList(options)
                .build();

        CaseData caseData = CaseData.builder()
                .ccdCaseReference(123L)
                .sdoR2SmallClaimsHearing(hearing)
                .build();

        CaseDataBuilder<?, ?> dataBuilder = CaseData.builder();

        DynamicList updatedOptions = DynamicList.builder()
                .listItems(List.of(
                        DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build()
                ))
                .build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        when(submitSdoUtils.deleteLocationList(any(DynamicList.class))).thenReturn(updatedOptions);

        try (MockedStatic<SdoHelper> mockedSdoHelper = mockStatic(SdoHelper.class)) {
            mockedSdoHelper.when(() -> SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)).thenReturn(true);

            sdoR2SmallClaimsHearingFieldUpdater.update(caseData, dataBuilder);
        }

        verify(submitSdoUtils, times(2)).deleteLocationList(any(DynamicList.class));

        CaseData updatedCaseData = dataBuilder.build();
        assertThat(updatedCaseData.getSdoR2SmallClaimsHearing().getHearingCourtLocationList()).isEqualTo(updatedOptions);
        assertThat(updatedCaseData.getSdoR2SmallClaimsHearing().getAltHearingCourtLocationList()).isEqualTo(updatedOptions);
    }

    @Test
    void shouldNotUpdateHearingFieldsWhenFeatureToggleIsDisabled() {
        CaseData caseData = CaseData.builder()
                .ccdCaseReference(123L)
                .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder().build())
                .build();

        CaseDataBuilder<?, ?> dataBuilder = CaseData.builder();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(false);

        sdoR2SmallClaimsHearingFieldUpdater.update(caseData, dataBuilder);

        verify(submitSdoUtils, never()).deleteLocationList(any(DynamicList.class));
    }
}
