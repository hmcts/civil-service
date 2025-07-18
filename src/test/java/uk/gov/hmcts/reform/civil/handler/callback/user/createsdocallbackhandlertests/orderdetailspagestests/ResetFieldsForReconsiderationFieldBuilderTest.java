package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.orderdetailspagestests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.orderdetailspages.ResetFieldsForReconsiderationFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SDOHearingNotes;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsImpNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsUploadDoc;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsAddNewDirections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ResetFieldsForReconsiderationFieldBuilderTest {

    @InjectMocks
    private ResetFieldsForReconsiderationFieldBuilder fieldBuilder;

    private CaseData.CaseDataBuilder<?, ?> caseDataBuilder;

    @BeforeEach
    void setUp() {

        DisposalHearingAddNewDirections disposalHearingAddNewDirections = DisposalHearingAddNewDirections.builder()
                .directionComment("test")
                .build();

        Element<DisposalHearingAddNewDirections> disposalHearingAddNewDirectionsElement =
                Element.<DisposalHearingAddNewDirections>builder()
                        .value(disposalHearingAddNewDirections)
                        .build();

        SmallClaimsAddNewDirections smallClaimsAddNewDirections = SmallClaimsAddNewDirections.builder()
                .directionComment("test")
                .build();

        Element<SmallClaimsAddNewDirections> smallClaimsAddNewDirectionsElement =
                Element.<SmallClaimsAddNewDirections>builder()
                        .value(smallClaimsAddNewDirections)
                        .build();

        FastTrackAddNewDirections fastTrackAddNewDirections = FastTrackAddNewDirections.builder()
                .directionComment("test")
                .build();

        Element<FastTrackAddNewDirections> fastTrackAddNewDirectionsElement =
                Element.<FastTrackAddNewDirections>builder()
                        .value(fastTrackAddNewDirections)
                        .build();

        caseDataBuilder = CaseData.builder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.YES)
                .fastClaims(new ArrayList<>())
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .orderType(OrderType.DISPOSAL)
                .trialAdditionalDirectionsForFastTrack(new ArrayList<>())
                .drawDirectionsOrderSmallClaimsAdditionalDirections(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .fastTrackAllocation(FastTrackAllocation.builder().assignComplexityBand(YesOrNo.YES).build())
                .disposalHearingAddNewDirections(List.of(disposalHearingAddNewDirectionsElement))
                .smallClaimsAddNewDirections(List.of(smallClaimsAddNewDirectionsElement))
                .fastTrackAddNewDirections(List.of(fastTrackAddNewDirectionsElement))
                .sdoHearingNotes(SDOHearingNotes.builder().input("test notes").build())
                .fastTrackHearingNotes(FastTrackHearingNotes.builder().input("test notes").build())
                .disposalHearingHearingNotes("Notes")
                .disposalHearingHearingNotes("test notes")
                .sdoR2SmallClaimsUploadDoc(SdoR2SmallClaimsUploadDoc.builder().build())
                .sdoR2SmallClaimsPPI(SdoR2SmallClaimsPPI.builder().build())
                .sdoR2SmallClaimsWitnessStatements(SdoR2SmallClaimsWitnessStatements.builder().build())
                .sdoR2SmallClaimsHearingToggle(Collections.singletonList(IncludeInOrderToggle.INCLUDE))
                .sdoR2SmallClaimsJudgesRecital(SdoR2SmallClaimsJudgesRecital.builder().build())
                .sdoR2SmallClaimsWitnessStatementsToggle(Collections.singletonList(IncludeInOrderToggle.INCLUDE))
                .sdoR2SmallClaimsPPIToggle(Collections.singletonList(IncludeInOrderToggle.INCLUDE))
                .sdoR2SmallClaimsImpNotes(SdoR2SmallClaimsImpNotes.builder().build())
                .sdoR2SmallClaimsUploadDocToggle(Collections.singletonList(IncludeInOrderToggle.INCLUDE));
    }

    @Test
    void shouldResetFieldsForReconsideration() {
        fieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();

        assertThat(caseData.getDrawDirectionsOrderRequired()).isNull();
        assertThat(caseData.getDrawDirectionsOrderSmallClaims()).isNull();
        assertThat(caseData.getFastClaims()).isNull();
        assertThat(caseData.getSmallClaims()).isNull();
        assertThat(caseData.getClaimsTrack()).isNull();
        assertThat(caseData.getOrderType()).isNull();
        assertThat(caseData.getTrialAdditionalDirectionsForFastTrack()).isNull();
        assertThat(caseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections()).isNull();
        assertThat(caseData.getFastTrackAllocation().getAssignComplexityBand()).isNull();
        assertThat(caseData.getDisposalHearingAddNewDirections()).isNull();
        assertThat(caseData.getSmallClaimsAddNewDirections()).isNull();
        assertThat(caseData.getFastTrackAddNewDirections()).isNull();
        assertThat(caseData.getSdoHearingNotes()).isNull();
        assertThat(caseData.getFastTrackHearingNotes()).isNull();
        assertThat(caseData.getDisposalHearingHearingNotes()).isNull();
        assertThat(caseData.getSdoR2SmallClaimsHearing()).isNull();
        assertThat(caseData.getSdoR2SmallClaimsUploadDoc()).isNull();
        assertThat(caseData.getSdoR2SmallClaimsPPI()).isNull();
        assertThat(caseData.getSdoR2SmallClaimsImpNotes()).isNull();
        assertThat(caseData.getSdoR2SmallClaimsWitnessStatements()).isNull();
        assertThat(caseData.getSdoR2SmallClaimsHearingToggle()).isNull();
        assertThat(caseData.getSdoR2SmallClaimsJudgesRecital()).isNull();
        assertThat(caseData.getSdoR2SmallClaimsWitnessStatementsToggle()).isNull();
        assertThat(caseData.getSdoR2SmallClaimsPPIToggle()).isNull();
        assertThat(caseData.getSdoR2SmallClaimsUploadDocToggle()).isNull();
    }
}