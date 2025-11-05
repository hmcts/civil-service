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
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;

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

        CaseData caseData = CaseData.builder().build();
        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.YES);
        assertThat(result.getSetFastTrackFlag()).isEqualTo(YesOrNo.NO);
        assertThat(result.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldEnableSmallClaimsWithoutNewScreenWhenNotDrh() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(true);
        when(caseClassificationService.isDrhSmallClaim(any())).thenReturn(false);

        CaseData result = service.updateOrderDetails(orderDetailsContext(CaseData.builder().build(), V_1));

        assertThat(result.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.YES);
        assertThat(result.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldEnableFastTrackFlagsForNihlFastTrack() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(true);
        when(caseClassificationService.isNihlFastTrack(any())).thenReturn(true);

        CaseData caseData = CaseData.builder().build();
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

        CaseData result = service.updateOrderDetails(orderDetailsContext(CaseData.builder().build(), V_1));

        assertThat(result.getSetFastTrackFlag()).isEqualTo(YesOrNo.YES);
        assertThat(result.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldResetFlagsWhenNoTrackMatches() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .setSmallClaimsFlag(YesOrNo.YES)
            .setFastTrackFlag(YesOrNo.YES)
            .isSdoR2NewScreen(YesOrNo.YES)
            .build();

        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.NO);
        assertThat(result.getSetFastTrackFlag()).isEqualTo(YesOrNo.NO);
        assertThat(result.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldApplyJudgementDeductionPercentageToAllTracks() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .drawDirectionsOrder(JudgementSum.builder().judgementSum(20d).build())
            .build();

        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getDisposalHearingJudgementDeductionValue().getValue()).isEqualTo("20.0%");
        assertThat(result.getFastTrackJudgementDeductionValue().getValue()).isEqualTo("20.0%");
        assertThat(result.getSmallClaimsJudgementDeductionValue().getValue()).isEqualTo("20.0%");
    }

    @Test
    void shouldMapHearingMethodListsWhenUsingV1() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(false);

        DynamicListElement inPerson = DynamicListElement.builder()
            .code("IN_PERSON")
            .label(HearingMethod.IN_PERSON.getLabel())
            .build();

        CaseData caseData = CaseData.builder()
            .hearingMethodValuesDisposalHearing(DynamicList.builder().value(inPerson).build())
            .build();

        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getDisposalHearingMethod()).isEqualTo(DisposalHearingMethod.disposalHearingMethodInPerson);
    }

    @Test
    void shouldMapFastTrackHearingWhenProvided() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(false);

        DynamicListElement telephone = DynamicListElement.builder()
            .code("TEL")
            .label(HearingMethod.TELEPHONE.getLabel())
            .build();
        CaseData caseData = CaseData.builder()
            .hearingMethodValuesFastTrack(DynamicList.builder().value(telephone).build())
            .build();

        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getFastTrackMethod()).isEqualTo(FastTrackMethod.fastTrackMethodTelephoneHearing);
    }

    @Test
    void shouldSkipMappingWhenNotVersionOne() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(false);

        DynamicListElement telephone = DynamicListElement.builder()
            .code("TEL")
            .label(HearingMethod.TELEPHONE.getLabel())
            .build();
        CaseData caseData = CaseData.builder()
            .hearingMethodValuesFastTrack(DynamicList.builder().value(telephone).build())
            .build();

        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_2));

        assertThat(result.getFastTrackMethod()).isNull();
    }

    @Test
    void shouldMapSmallClaimsHearingListWhenProvided() {
        when(caseClassificationService.isSmallClaimsTrack(any())).thenReturn(false);
        when(caseClassificationService.isFastTrack(any())).thenReturn(false);

        DynamicListElement video = DynamicListElement.builder()
            .code("VIDEO")
            .label(HearingMethod.VIDEO.getLabel())
            .build();
        CaseData caseData = CaseData.builder()
            .hearingMethodValuesSmallClaims(DynamicList.builder().value(video).build())
            .build();

        CaseData result = service.updateOrderDetails(orderDetailsContext(caseData, V_1));

        assertThat(result.getSmallClaimsMethod()).isEqualTo(SmallClaimsMethod.smallClaimsMethodVideoConferenceHearing);
    }

    private SdoTaskContext orderDetailsContext(CaseData caseData, CallbackVersion version) {
        CallbackParams params = CallbackParams.builder()
            .params(Collections.emptyMap())
            .version(version)
            .build();

        return new SdoTaskContext(caseData, params, SdoLifecycleStage.ORDER_DETAILS);
    }
}
