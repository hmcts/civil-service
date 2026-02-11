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
import uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderCaseProgressionService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        CaseDocument document = new CaseDocument();
        document.setDocumentName("sdo.pdf");
        List<Element<CaseDocument>> generatedDocs = new ArrayList<>();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setSdoOrderDocument(document);
        caseData.setSystemGeneratedCaseDocuments(generatedDocs);

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
        CaseDocument document = new CaseDocument();
        document.setDocumentName("sdo.pdf");
        List<Element<CaseDocument>> preTranslationDocs = new ArrayList<>();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setSdoOrderDocument(document);
        caseData.setPreTranslationDocuments(preTranslationDocs);

        when(featureToggleService.isWelshJourneyEnabled(caseData)).thenReturn(true);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getPreTranslationDocuments()).hasSize(1);
        assertThat(result.getPreTranslationDocuments().get(0).getValue()).isEqualTo(document);
        assertThat(result.getSdoOrderDocument()).isNull();
    }

    @Test
    void shouldSetEaCourtLocationForSpecCaseWhenNotLip() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation("123"));

        mockEaCourtMutation(caseData, YesOrNo.YES);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getEaCourtLocation()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldRespectLipCaseWhenCourtNotWhiteListed() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation("123"));

        mockEaCourtMutation(caseData, YesOrNo.NO);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getEaCourtLocation()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldTrimDynamicListsAndSmallClaimsHearingLocations() {
        DynamicListElement selected = new DynamicListElement();
        selected.setCode("loc");
        selected.setLabel("Loc");
        DynamicList originalList = new DynamicList();
        originalList.setValue(selected);
        DynamicList trimmedList = new DynamicList();
        trimmedList.setValue(selected);
        trimmedList.setListItems(List.of());

        when(locationService.trimListItems(originalList)).thenReturn(trimmedList);
        when(locationService.trimListItems(originalList)).thenReturn(trimmedList);
        when(classificationService.isDrhSmallClaim(any())).thenReturn(true);

        SdoR2SmallClaimsHearing hearing = new SdoR2SmallClaimsHearing();
        hearing.setHearingCourtLocationList(originalList);
        hearing.setAltHearingCourtLocationList(originalList);
        SdoR2Trial trial = new SdoR2Trial();
        trial.setHearingCourtLocationList(originalList);
        trial.setAltHearingCourtLocationList(originalList);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);
        caseData.setDisposalHearingMethodInPerson(originalList);
        caseData.setFastTrackMethodInPerson(originalList);
        caseData.setSmallClaimsMethodInPerson(originalList);
        caseData.setSdoR2SmallClaimsHearing(hearing);
        caseData.setSdoR2Trial(trial);

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);

        when(classificationService.isSmallClaimsTrack(caseData)).thenReturn(true);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getAllocatedTrack()).isEqualTo(uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM);
    }

    @Test
    void shouldUpdateClaimsTrackForSpecFastTrack() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);

        when(classificationService.isSmallClaimsTrack(caseData)).thenReturn(false);
        when(classificationService.isFastTrack(caseData)).thenReturn(true);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getResponseClaimTrack()).isEqualTo("FAST_CLAIM");
    }

    @Test
    void shouldDelegateWaLocationsToHelper() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

        service.prepareSubmission(caseData, AUTH_TOKEN);

        verify(caseProgressionService).applyCaseProgressionRouting(caseData, AUTH_TOKEN, false, true);
    }

    private void mockEaCourtMutation(CaseData caseData, YesOrNo value) {
        doAnswer(invocation -> {
            caseData.setEaCourtLocation(value);
            return null;
        }).when(caseProgressionService).applyCaseProgressionRouting(caseData, AUTH_TOKEN, false, true);
    }

}
