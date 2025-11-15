package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderCaseProgressionService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoSubmissionServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private SdoFeatureToggleService featureToggleService;
    @Mock
    private SdoLocationService locationService;
    @Mock
    private DirectionsOrderCaseProgressionService caseProgressionService;
    @Mock
    private SdoCaseClassificationService classificationService;

    private SdoSubmissionService service;

    @BeforeEach
    void setUp() {
        service = new SdoSubmissionService(featureToggleService, locationService, caseProgressionService, classificationService);
    }

    @Test
    void shouldMoveDocumentToSystemGeneratedWhenEnglishJourney() {
        CaseDocument document = CaseDocumentBuilder.builder().documentName("sdo.pdf").build();
        List<Element<CaseDocument>> generatedDocs = new ArrayList<>();
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .sdoOrderDocument(document)
            .systemGeneratedCaseDocuments(generatedDocs)
            .build();

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getSystemGeneratedCaseDocuments()).hasSize(1);
        assertThat(result.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(document);
        assertThat(result.getSdoOrderDocument()).isNull();
        assertThat(result.getBusinessProcess()).isNotNull();
        assertThat(result.getBusinessProcess().getCamundaEvent()).isEqualTo(CaseEvent.CREATE_SDO.name());
        assertThat(result.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
    }

    @Test
    void shouldMoveDocumentToPreTranslationForWelshJourney() {
        CaseDocument document = CaseDocumentBuilder.builder().documentName("sdo.pdf").build();
        List<Element<CaseDocument>> preTranslationDocs = new ArrayList<>();
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .sdoOrderDocument(document)
            .preTranslationDocuments(preTranslationDocs)
            .build();

        when(featureToggleService.isWelshJourneyEnabled(caseData)).thenReturn(true);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getPreTranslationDocuments()).hasSize(1);
        assertThat(result.getPreTranslationDocuments().get(0).getValue()).isEqualTo(document);
        assertThat(result.getSdoOrderDocument()).isNull();
    }

    @Test
    void shouldSetEaCourtLocationForSpecCaseWhenNotLip() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("123").build())
            .build();

        mockEaCourtMutation(caseData, YesOrNo.YES);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getEaCourtLocation()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldRespectLipCaseWhenCourtNotWhiteListed() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("123").build())
            .build();

        mockEaCourtMutation(caseData, YesOrNo.NO);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getEaCourtLocation()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldTrimDynamicListsAndSmallClaimsHearingLocations() {
        DynamicListElement selected = DynamicListElement.builder().code("loc").label("Loc").build();
        DynamicList originalList = DynamicList.builder().value(selected).build();
        DynamicList trimmedList = DynamicList.builder().value(selected).listItems(List.of()).build();

        when(locationService.trimListItems(originalList)).thenReturn(trimmedList);
        when(locationService.trimListItems(originalList)).thenReturn(trimmedList);
        when(classificationService.isDrhSmallClaim(any())).thenReturn(true);

        SdoR2SmallClaimsHearing hearing = SdoR2SmallClaimsHearing.builder()
            .hearingCourtLocationList(originalList)
            .altHearingCourtLocationList(originalList)
            .build();
        SdoR2Trial trial = SdoR2Trial.builder()
            .hearingCourtLocationList(originalList)
            .altHearingCourtLocationList(originalList)
            .build();

        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .disposalHearingMethodInPerson(originalList)
            .fastTrackMethodInPerson(originalList)
            .smallClaimsMethodInPerson(originalList)
            .sdoR2SmallClaimsHearing(hearing)
            .sdoR2Trial(trial)
            .build();

        when(locationService.trimListItems(originalList)).thenReturn(trimmedList);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getDisposalHearingMethodInPerson()).isEqualTo(trimmedList);
        assertThat(result.getFastTrackMethodInPerson()).isEqualTo(trimmedList);
        assertThat(result.getSmallClaimsMethodInPerson()).isEqualTo(trimmedList);
        assertThat(result.getSdoR2SmallClaimsHearing().getHearingCourtLocationList()).isEqualTo(trimmedList);
        assertThat(result.getSdoR2SmallClaimsHearing().getAltHearingCourtLocationList()).isEqualTo(trimmedList);
        assertThat(result.getSdoR2Trial().getHearingCourtLocationList()).isEqualTo(trimmedList);
        assertThat(result.getSdoR2Trial().getAltHearingCourtLocationList()).isEqualTo(trimmedList);
    }

    @Test
    void shouldUpdateClaimsTrackForUnspecSmallClaims() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .build();

        when(classificationService.isSmallClaimsTrack(caseData)).thenReturn(true);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getAllocatedTrack()).isEqualTo(uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM);
    }

    @Test
    void shouldUpdateClaimsTrackForSpecFastTrack() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();

        when(classificationService.isSmallClaimsTrack(caseData)).thenReturn(false);
        when(classificationService.isFastTrack(caseData)).thenReturn(true);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getResponseClaimTrack()).isEqualTo("FAST_CLAIM");
    }

    @Test
    void shouldDelegateWaLocationsToHelper() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

        service.prepareSubmission(caseData, AUTH_TOKEN);

        verify(caseProgressionService).applyCaseProgressionRouting(eq(caseData), any(), eq(AUTH_TOKEN));
    }

    private void mockEaCourtMutation(CaseData caseData, YesOrNo value) {
        doAnswer(invocation -> {
            CaseData.CaseDataBuilder<?, ?> builder = invocation.getArgument(1);
            builder.eaCourtLocation(value);
            return null;
        }).when(caseProgressionService).applyCaseProgressionRouting(eq(caseData), any(), eq(AUTH_TOKEN));
    }

}
