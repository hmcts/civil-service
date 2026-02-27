package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;

@ExtendWith(MockitoExtension.class)
class SdoOrderDetailsServiceTest {

    @Mock
    private SdoCaseClassificationService caseClassificationService;

    private SdoOrderDetailsService service;

    @BeforeEach
    void setUp() {
        service = new SdoOrderDetailsService(caseClassificationService);
    }

    @Test
    void shouldEnableSmallClaimsFlagsForDrhSmallClaim() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(true);
        when(caseClassificationService.isDrhSmallClaim(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.YES);
        assertThat(result.getSetFastTrackFlag()).isEqualTo(YesOrNo.NO);
        assertThat(result.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldEnableSmallClaimsWithoutNewScreenWhenNotDrh() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(true);
        when(caseClassificationService.isDrhSmallClaim(any())).thenReturn(false);

        CaseData result = service.updateOrderDetails(orderDetailsContext(CaseDataBuilder.builder().build(), V_1)).build();

        assertThat(result.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.YES);
        assertThat(result.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldEnableFastTrackFlagsForNihlFastTrack() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(true);
        when(caseClassificationService.isNihlFastTrack(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.NO);
        assertThat(result.getSetFastTrackFlag()).isEqualTo(YesOrNo.YES);
        assertThat(result.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldDisableNewScreenForFastTrackWhenNotNihl() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(true);
        when(caseClassificationService.isNihlFastTrack(any())).thenReturn(false);

        CaseData result = service.updateOrderDetails(orderDetailsContext(CaseDataBuilder.builder().build(), V_1)).build();

        assertThat(result.getSetFastTrackFlag()).isEqualTo(YesOrNo.YES);
        assertThat(result.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldResetFlagsWhenNoTrackMatches() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSetSmallClaimsFlag(YesOrNo.YES);
        caseData.setSetFastTrackFlag(YesOrNo.YES);
        caseData.setIsSdoR2NewScreen(YesOrNo.YES);

        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.NO);
        assertThat(result.getSetFastTrackFlag()).isEqualTo(YesOrNo.NO);
        assertThat(result.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldApplyJudgementDeductionPercentageToAllTracks() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        JudgementSum judgementSum = new JudgementSum();
        judgementSum.setJudgementSum(20d);
        caseData.setDrawDirectionsOrder(judgementSum);

        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getDisposalHearingJudgementDeductionValue().getValue()).isEqualTo("20.0%");
        assertThat(result.getFastTrackJudgementDeductionValue().getValue()).isEqualTo("20.0%");
        assertThat(result.getSmallClaimsJudgementDeductionValue().getValue()).isEqualTo("20.0%");
    }

    @Test
    void shouldMapHearingMethodListsWhenUsingV1() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(false);

        DynamicListElement inPerson = new DynamicListElement();
        inPerson.setCode("IN_PERSON");
        inPerson.setLabel(HearingMethod.IN_PERSON.getLabel());
        DynamicList disposalList = new DynamicList();
        disposalList.setValue(inPerson);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setHearingMethodValuesDisposalHearing(disposalList);

        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getDisposalHearingMethod()).isEqualTo(DisposalHearingMethod.disposalHearingMethodInPerson);
    }

    @Test
    void shouldMapFastTrackHearingWhenProvided() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(false);

        DynamicListElement telephone = new DynamicListElement();
        telephone.setCode("TEL");
        telephone.setLabel(HearingMethod.TELEPHONE.getLabel());
        DynamicList fastTrackList = new DynamicList();
        fastTrackList.setValue(telephone);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setHearingMethodValuesFastTrack(fastTrackList);

        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getFastTrackMethod()).isEqualTo(FastTrackMethod.fastTrackMethodTelephoneHearing);
    }

    @Test
    void shouldSkipMappingWhenNotVersionOne() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(false);

        DynamicListElement telephone = new DynamicListElement();
        telephone.setCode("TEL");
        telephone.setLabel(HearingMethod.TELEPHONE.getLabel());
        DynamicList fastTrackList = new DynamicList();
        fastTrackList.setValue(telephone);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setHearingMethodValuesFastTrack(fastTrackList);

        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_2));

        assertThat(result.getFastTrackMethod()).isNull();
    }

    @Test
    void shouldMapSmallClaimsHearingListWhenProvided() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(false);

        DynamicListElement video = new DynamicListElement();
        video.setCode("VIDEO");
        video.setLabel(HearingMethod.VIDEO.getLabel());
        DynamicList smallClaimsList = new DynamicList();
        smallClaimsList.setValue(video);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setHearingMethodValuesSmallClaims(smallClaimsList);

        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getSmallClaimsMethod()).isEqualTo(SmallClaimsMethod.smallClaimsMethodVideoConferenceHearing);
    }

    private DirectionsOrderTaskContext orderDetailsContext(CaseData caseData, CallbackVersion version) {
        CallbackParams params = new CallbackParams()
            .params(Collections.emptyMap())
            .version(version);

        return new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.ORDER_DETAILS);
    }
}
