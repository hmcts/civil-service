package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormDisposal;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFast;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmall;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmallDrh;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurtherDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ExpertEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfPagesDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2UploadOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDisposalDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFastTrackDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFastTrackSpecialistDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFastTrackTemplateFieldService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoR2SmallClaimsDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoNihlTemplateFieldService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoR2TrialTemplateFieldService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsTemplateFieldService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoMediationSectionService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDeadlineService;
import uk.gov.hmcts.reform.civil.utils.HearingMethodUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_FAST_FAST_TRACK_INT_R2;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_FAST_TRACK_NIHL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_R2_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_SMALL_DRH;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_SMALL_R2;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_COLUMNS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_INTRO_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_BUNDLE_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_DOCUMENTS_HEADING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_NOTES_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_PARTIES_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FLIGHT_DELAY_LEGAL_ARGUMENTS_NOTICE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FLIGHT_DELAY_RELATED_CLAIMS_NOTICE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    SdoGeneratorService.class,
    SdoCaseClassificationService.class,
    SdoDisposalDirectionsService.class,
    SdoFastTrackDirectionsService.class,
    SdoFastTrackTemplateFieldService.class,
    SdoFastTrackTemplateService.class,
    SdoNihlTemplateService.class,
    SdoNihlTemplateFieldService.class,
    SdoDisposalTemplateService.class,
    SdoSmallClaimsDirectionsService.class,
    SdoSmallClaimsTemplateFieldService.class,
    SdoMediationSectionService.class,
    SdoSmallClaimsDrhTemplateService.class,
    SdoSmallClaimsTemplateService.class,
    SdoR2TrialTemplateFieldService.class,
    SdoR2SmallClaimsDirectionsService.class,
    JacksonAutoConfiguration.class
})
public class SdoGeneratorServiceTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static String fileNameSmall = null;
    private static String fileNameFast = null;
    private static String fileNameDisposal = null;
    private static String fileNameSmallDrh = null;
    private static final CaseDocument CASE_DOCUMENT_SMALL = CaseDocumentBuilder.builder()
        .documentName(fileNameSmall)
        .documentType(SDO_ORDER)
        .build();
    private static final CaseDocument CASE_DOCUMENT_FAST = CaseDocumentBuilder.builder()
        .documentName(fileNameFast)
        .documentType(SDO_ORDER)
        .build();
    private static final CaseDocument CASE_DOCUMENT_DISPOSAL = CaseDocumentBuilder.builder()
        .documentName(fileNameDisposal)
        .documentType(SDO_ORDER)
        .build();

    private static final CaseDocument CASE_DOCUMENT_SMALL_DRH = CaseDocumentBuilder.builder()
        .documentName(fileNameSmallDrh)
        .documentType(SDO_ORDER)
        .build();

    @MockBean
    private SecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    protected UserService userService;

    @MockBean
    private DocumentHearingLocationHelper documentHearingLocationHelper;

    @Autowired
    private SdoGeneratorService generator;

    @Mock
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        fileNameDisposal = LocalDate.now() + "_Judgey McJudge" + ".pdf";
        fileNameFast = LocalDate.now() + "_Judgey McJudge" + ".pdf";
        fileNameSmall = LocalDate.now() + "_Judgey McJudge" + ".pdf";
        fileNameSmallDrh = LocalDate.now() + "_Judgey McJudge" + ".pdf";

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .forename("Judgey")
                                                                     .surname("McJudge")
                                                                     .roles(Collections.emptyList()).build());
    }

    @Test
    public void sdoSmall() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_SMALL_R2)))
            .thenReturn(new DocmosisDocument(SDO_SMALL_R2.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameSmall, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_SMALL);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.smallClaimsTrack)
            .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameSmall, bytes, SDO_ORDER));
    }

    @Test
    public void sdoSmallInPerson() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_SMALL_R2)))
            .thenReturn(new DocmosisDocument(SDO_SMALL_R2.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameSmall, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_SMALL);

        LocationRefData locationRefData = new LocationRefData();
        String locationLabel = "String 1";
        DynamicList formValue = DynamicList.fromList(
            Collections.singletonList(locationLabel),
            Object::toString,
            locationLabel,
            false
        );
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.smallClaimsTrack)
            .smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson)
            .smallClaimsMethodInPerson(formValue)
            .build();
        when(documentHearingLocationHelper.getHearingLocation(locationLabel, caseData, BEARER_TOKEN))
            .thenReturn(locationRefData);

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameSmall, bytes, SDO_ORDER));
        verify(documentGeneratorService).generateDocmosisDocument(
            argThat((MappableObject templateData) ->
                        templateData instanceof SdoDocumentFormSmall
                            && locationRefData.equals(((SdoDocumentFormSmall) templateData).getHearingLocation())),
            any(DocmosisTemplates.class)
        );
    }

    @Test
    void sdoSmallFlightDelay() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_SMALL_R2)))
            .thenReturn(new DocmosisDocument(SDO_SMALL_R2.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameSmall, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_SMALL);

        LocationRefData locationRefData = new LocationRefData();
        String locationLabel = "String 1";
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atSmallSmallClaimsFlightDelayInputs()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.smallClaimsTrack)
            .smallClaims(List.of(SmallTrack.smallClaimFlightDelay))
            .smallClaimsFlightDelayToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .build();
        when(documentHearingLocationHelper.getHearingLocation(locationLabel, caseData, BEARER_TOKEN))
            .thenReturn(locationRefData);

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameSmall, bytes, SDO_ORDER));
        verify(documentGeneratorService).generateDocmosisDocument(
            argThat((MappableObject templateData) ->
                        templateData instanceof SdoDocumentFormSmall),
            any(DocmosisTemplates.class)
        );

        ArgumentCaptor<SdoDocumentFormSmall> argument = ArgumentCaptor.forClass(SdoDocumentFormSmall.class);
        verify(documentGeneratorService).generateDocmosisDocument(argument.capture(), any(DocmosisTemplates.class));
        assertThat(argument.getValue().getSmallClaimsFlightDelay()).isNotNull();
        assertThat(argument.getValue().getSmallClaimsFlightDelay().getRelatedClaimsInput())
            .isEqualTo(FLIGHT_DELAY_RELATED_CLAIMS_NOTICE);
        assertThat(argument.getValue().getSmallClaimsFlightDelay().getLegalDocumentsInput())
            .isEqualTo(FLIGHT_DELAY_LEGAL_ARGUMENTS_NOTICE);
    }

    @Test
    public void sdoSmallInPersonCarm() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_SMALL_R2)))
            .thenReturn(new DocmosisDocument(SDO_SMALL_R2.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameSmall, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_SMALL);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        LocationRefData locationRefData = new LocationRefData();
        String locationLabel = "String 1";
        DynamicList formValue = DynamicList.fromList(
            Collections.singletonList(locationLabel),
            Object::toString,
            locationLabel,
            false
        );
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.smallClaimsTrack)
            .smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson)
            .smallClaimsMethodInPerson(formValue)
            .build().toBuilder()
            .smallClaimsMediationSectionStatement(
                new SmallClaimsMediation().setInput("mediation representation")
            )
            .build();
        when(documentHearingLocationHelper.getHearingLocation(locationLabel, caseData, BEARER_TOKEN))
            .thenReturn(locationRefData);

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameSmall, bytes, SDO_ORDER));
        verify(documentGeneratorService).generateDocmosisDocument(
            argThat((MappableObject templateData) ->
                        templateData instanceof SdoDocumentFormSmall
                            && ((SdoDocumentFormSmall) templateData).getSmallClaimMediationSectionInput()
                            .equals("mediation representation")),
            any(DocmosisTemplates.class)
        );
    }

    @Test
    public void shouldGenerateSdoFastTrackDocumentWithR2TemplateAndFastTrackUpLiftsEnabled() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_FAST_FAST_TRACK_INT_R2)))
            .thenReturn(new DocmosisDocument(SDO_FAST_FAST_TRACK_INT_R2.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_FAST);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateSdoFastTrackTrial()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER));
        verify(documentGeneratorService).generateDocmosisDocument(
            argThat((MappableObject templateData) ->
                        templateData instanceof SdoDocumentFormFast),
            eq(SDO_FAST_FAST_TRACK_INT_R2)
        );
    }

    @Test
    void shouldPropagateSpecialistTextIntoFastTrackDocmosisTemplate() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_FAST_FAST_TRACK_INT_R2)))
            .thenReturn(new DocmosisDocument(SDO_FAST_FAST_TRACK_INT_R2.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_FAST);

        CaseData baseCase = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateSdoFastTrackTrial()
            .build();

        CaseData caseData = fastTrackCasePopulatedBySpecialistService(baseCase);

        generator.generate(caseData, BEARER_TOKEN);

        ArgumentCaptor<SdoDocumentFormFast> captor = ArgumentCaptor.forClass(SdoDocumentFormFast.class);
        verify(documentGeneratorService).generateDocmosisDocument(captor.capture(), eq(SDO_FAST_FAST_TRACK_INT_R2));
        SdoDocumentFormFast form = captor.getValue();

        assertThat(form.getFastTrackBuildingDispute())
            .extracting(FastTrackBuildingDispute::getInput1, FastTrackBuildingDispute::getInput2,
                        FastTrackBuildingDispute::getInput3, FastTrackBuildingDispute::getInput4)
            .containsExactly(
                BUILDING_SCHEDULE_INTRO_SDO,
                BUILDING_SCHEDULE_COLUMNS_SDO,
                BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION,
                BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION
            );
        assertThat(form.getFastTrackClinicalNegligence())
            .extracting(FastTrackClinicalNegligence::getInput1, FastTrackClinicalNegligence::getInput2,
                        FastTrackClinicalNegligence::getInput3, FastTrackClinicalNegligence::getInput4)
            .containsExactly(
                CLINICAL_DOCUMENTS_HEADING,
                CLINICAL_PARTIES_SDO,
                CLINICAL_NOTES_SDO,
                CLINICAL_BUNDLE_SDO
            );
        assertThat(form.isShowBundleInfo()).isTrue();
    }

    private CaseData fastTrackCasePopulatedBySpecialistService(CaseData baseCase) {
        SdoDeadlineService deadlineService = Mockito.mock(SdoDeadlineService.class);
        when(deadlineService.nextWorkingDayFromNowWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 1, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));

        SdoFastTrackSpecialistDirectionsService specialistService =
            new SdoFastTrackSpecialistDirectionsService(deadlineService);

        CaseData caseData = baseCase.toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .fastTrackTrialBundleToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .build();

        specialistService.populateSpecialistDirections(caseData);
        return caseData;
    }

    @Test
    public void shouldGenerateSdoDisposalDocument() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_R2_DISPOSAL)))
            .thenReturn(new DocmosisDocument(SDO_R2_DISPOSAL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameDisposal, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_DISPOSAL);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateSdoDisposal()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(YesOrNo.NO)
            .orderType(OrderType.DISPOSAL)
            .claimsTrack(ClaimsTrack.fastTrack)
            .build();

        LocationRefData locationRefData = new LocationRefData();
        Mockito.when(documentHearingLocationHelper.getHearingLocation(
            nullable(String.class), eq(caseData), eq(BEARER_TOKEN)
        )).thenReturn(locationRefData);

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameDisposal, bytes, SDO_ORDER));
        verify(documentGeneratorService).generateDocmosisDocument(
            argThat((MappableObject arg) ->
                        arg instanceof SdoDocumentFormDisposal
                            && locationRefData.equals(((SdoDocumentFormDisposal) arg).getHearingLocation())
            ),
            any(DocmosisTemplates.class)
        );
    }

    @Test
    public void shouldGenerateSdoDisposalDocumentInPerson() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_R2_DISPOSAL)))
            .thenReturn(new DocmosisDocument(SDO_R2_DISPOSAL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameDisposal, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_DISPOSAL);

        LocationRefData locationRefData = new LocationRefData();
        String locationLabel = "String 1";
        DynamicList formValue = DynamicList.fromList(
            Collections.singletonList(locationLabel),
            Object::toString,
            locationLabel,
            false
        );
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateSdoDisposal()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(YesOrNo.NO)
            .orderType(OrderType.DISPOSAL)
            .claimsTrack(ClaimsTrack.fastTrack)
            .disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson)
            .disposalHearingMethodInPerson(formValue)
            .build();
        when(documentHearingLocationHelper.getHearingLocation(locationLabel, caseData, BEARER_TOKEN))
            .thenReturn(locationRefData);

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameDisposal, bytes, SDO_ORDER));
        verify(documentGeneratorService).generateDocmosisDocument(
            argThat((MappableObject arg) ->
                        arg instanceof SdoDocumentFormDisposal
                            && locationRefData.equals(((SdoDocumentFormDisposal) arg).getHearingLocation())
            ),
            any(DocmosisTemplates.class)
        );
    }

    @Test
    public void shouldGenerateSdoFastTrackNihlDocument_pathone() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_FAST_TRACK_NIHL)))
            .thenReturn(new DocmosisDocument(SDO_FAST_TRACK_NIHL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_FAST);
        List<FastTrack> fastTrackList = new ArrayList<>();
        fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDraft()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .fastClaims(fastTrackList)
            .build();

        caseData  = prePopulateNihlFields(caseData.toBuilder());
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER));
    }

    @Test
    public void shouldGenerateSdoFastTrackNihlDocument_pathtwo() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_FAST_TRACK_NIHL)))
            .thenReturn(new DocmosisDocument(SDO_FAST_TRACK_NIHL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_FAST);
        List<FastTrack> fastTrackList = new ArrayList<>();
        fastTrackList.add(FastTrack.fastClaimBuildingDispute);
        fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDraft()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(YesOrNo.NO)
            .orderType(OrderType.DECIDE_DAMAGES)
            .claimsTrack(ClaimsTrack.fastTrack)
            .trialAdditionalDirectionsForFastTrack(fastTrackList)
            .build();

        caseData  = prePopulateNihlFields(caseData.toBuilder());
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        //assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER));
    }

    private CaseData prePopulateNihlFields(CaseData.CaseDataBuilder<?, ?> updatedData) {

        Category inPerson = new Category();
        inPerson.setCategoryKey("HearingChannel");
        inPerson.setKey("INTER");
        inPerson.setValueEn("In Person");
        inPerson.setActiveFlag("Y");
        Category video = new Category();
        video.setCategoryKey("HearingChannel");
        video.setKey("VID");
        video.setValueEn("Video");
        video.setActiveFlag("Y");
        Category telephone = new Category();
        telephone.setCategoryKey("HearingChannel");
        telephone.setKey("TEL");
        telephone.setValueEn("Telephone");
        telephone.setActiveFlag("Y");
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(List.of(inPerson, video, telephone));
        when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), eq("HearingChannel"), anyString())).thenReturn(
            Optional.of(categorySearchResult));

        DynamicListElement selectedCourt = new DynamicListElement();
        selectedCourt.setCode("00002");
        selectedCourt.setLabel("court 2 - 2 address - Y02 7RB");
        updatedData.sdoFastTrackJudgesRecital(new FastTrackJudgesRecital()
                                                  .setInput(SdoR2UiConstantFastTrack.JUDGE_RECITAL));
        SdoR2DisclosureOfDocuments disclosureOfDocuments = new SdoR2DisclosureOfDocuments();
        disclosureOfDocuments.setStandardDisclosureTxt(SdoR2UiConstantFastTrack.STANDARD_DISCLOSURE);
        disclosureOfDocuments.setStandardDisclosureDate(LocalDate.now().plusDays(28));
        disclosureOfDocuments.setInspectionTxt(SdoR2UiConstantFastTrack.INSPECTION);
        disclosureOfDocuments.setInspectionDate(LocalDate.now().plusDays(42));
        disclosureOfDocuments.setRequestsWillBeCompiledLabel(SdoR2UiConstantFastTrack.REQUEST_COMPILED_WITH);
        updatedData.sdoR2DisclosureOfDocuments(disclosureOfDocuments);
        SdoR2RestrictNoOfWitnessDetails restrictWitnessDetails = new SdoR2RestrictNoOfWitnessDetails();
        restrictWitnessDetails.setNoOfWitnessClaimant(3);
        restrictWitnessDetails.setNoOfWitnessDefendant(3);
        restrictWitnessDetails.setPartyIsCountedAsWitnessTxt(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT);
        SdoR2RestrictWitness restrictWitness = new SdoR2RestrictWitness();
        restrictWitness.setIsRestrictWitness(NO);
        restrictWitness.setRestrictNoOfWitnessDetails(restrictWitnessDetails);

        SdoR2RestrictNoOfPagesDetails restrictPagesDetails = new SdoR2RestrictNoOfPagesDetails();
        restrictPagesDetails.setWitnessShouldNotMoreThanTxt(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1);
        restrictPagesDetails.setNoOfPages(12);
        restrictPagesDetails.setFontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2);
        SdoR2RestrictPages restrictPages = new SdoR2RestrictPages();
        restrictPages.setIsRestrictPages(NO);
        restrictPages.setRestrictNoOfPagesDetails(restrictPagesDetails);

        SdoR2WitnessOfFact witnessOfFact = new SdoR2WitnessOfFact();
        witnessOfFact.setSdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS);
        witnessOfFact.setSdoR2RestrictWitness(restrictWitness);
        witnessOfFact.setSdoRestrictPages(restrictPages);
        witnessOfFact.setSdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE);
        witnessOfFact.setSdoWitnessDeadlineDate(LocalDate.now().plusDays(70));
        witnessOfFact.setSdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE);
        updatedData.sdoR2WitnessesOfFact(witnessOfFact);

        SdoR2ScheduleOfLoss scheduleOfLoss = new SdoR2ScheduleOfLoss();
        scheduleOfLoss.setSdoR2ScheduleOfLossClaimantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT);
        scheduleOfLoss.setIsClaimForPecuniaryLoss(NO);
        scheduleOfLoss.setSdoR2ScheduleOfLossClaimantDate(LocalDate.now().plusDays(364));
        scheduleOfLoss.setSdoR2ScheduleOfLossDefendantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT);
        scheduleOfLoss.setSdoR2ScheduleOfLossDefendantDate(LocalDate.now().plusDays(378));
        scheduleOfLoss.setSdoR2ScheduleOfLossPecuniaryLossTxt(SdoR2UiConstantFastTrack.PECUNIARY_LOSS);
        updatedData.sdoR2ScheduleOfLoss(scheduleOfLoss);
        DynamicListElement optionOne = new DynamicListElement();
        optionOne.setCode("00001");
        optionOne.setLabel("court 1 - 1 address - Y01 7RB");
        DynamicListElement optionTwo = new DynamicListElement();
        optionTwo.setCode("00002");
        optionTwo.setLabel("court 2 - 2 address - Y02 7RB");
        DynamicListElement optionThree = new DynamicListElement();
        optionThree.setCode("00003");
        optionThree.setLabel("court 3 - 3 address - Y03 7RB");
        DynamicList options = new DynamicList();
        options.setListItems(List.of(optionOne, optionTwo, optionThree));
        SdoR2TrialFirstOpenDateAfter trialFirstOpen = new SdoR2TrialFirstOpenDateAfter();
        trialFirstOpen.setListFrom(LocalDate.now().plusDays(434));
        SdoR2TrialWindow trialWindow = new SdoR2TrialWindow();
        trialWindow.setListFrom(LocalDate.now().plusDays(434));
        trialWindow.setDateTo(LocalDate.now().plusDays(455));
        SdoR2Trial trial = new SdoR2Trial();
        trial.setTrialOnOptions(TrialOnRadioOptions.OPEN_DATE);
        trial.setLengthList(FastTrackHearingTimeEstimate.FIVE_HOURS);
        trial.setMethodOfHearing(HearingMethodUtils.getHearingMethodList(categorySearchResult));
        trial.setPhysicalBundleOptions(PhysicalTrialBundleOptions.NONE);
        trial.setSdoR2TrialFirstOpenDateAfter(trialFirstOpen);
        trial.setSdoR2TrialWindow(trialWindow);
        trial.setHearingCourtLocationList(options.toBuilder().value(selectedCourt).build());
        trial.setPhysicalBundlePartyTxt(SdoR2UiConstantFastTrack.PHYSICAL_TRIAL_BUNDLE);
        updatedData.sdoR2Trial(trial);

        updatedData.sdoR2ImportantNotesTxt(SdoR2UiConstantFastTrack.IMPORTANT_NOTES);
        updatedData.sdoR2ImportantNotesDate(LocalDate.now().plusDays(7));

        SdoR2ExpertEvidence expertEvidence = new SdoR2ExpertEvidence();
        expertEvidence.setSdoClaimantPermissionToRelyTxt(SdoR2UiConstantFastTrack.CLAIMANT_PERMISSION_TO_RELY);
        updatedData.sdoR2ExpertEvidence(expertEvidence);
        SdoR2AddendumReport addendumReport = new SdoR2AddendumReport();
        addendumReport.setSdoAddendumReportTxt(SdoR2UiConstantFastTrack.ADDENDUM_REPORT);
        addendumReport.setSdoAddendumReportDate(LocalDate.now().plusDays(56));
        updatedData.sdoR2AddendumReport(addendumReport);
        SdoR2FurtherAudiogram furtherAudiogram = new SdoR2FurtherAudiogram();
        furtherAudiogram.setSdoClaimantShallUndergoTxt(SdoR2UiConstantFastTrack.CLAIMANT_SHALL_UNDERGO);
        furtherAudiogram.setSdoServiceReportTxt(SdoR2UiConstantFastTrack.SERVICE_REPORT);
        furtherAudiogram.setSdoClaimantShallUndergoDate(LocalDate.now().plusDays(42));
        furtherAudiogram.setSdoServiceReportDate(LocalDate.now().plusDays(98));
        updatedData.sdoR2FurtherAudiogram(furtherAudiogram);
        SdoR2ApplicationToRelyOnFurtherDetails applicationDetails = new SdoR2ApplicationToRelyOnFurtherDetails();
        applicationDetails.setApplicationToRelyDetailsTxt(SdoR2UiConstantFastTrack.APPLICATION_TO_RELY_DETAILS);
        applicationDetails.setApplicationToRelyDetailsDate(LocalDate.now().plusDays(161));
        SdoR2ApplicationToRelyOnFurther applicationToRelyOnFurther = new SdoR2ApplicationToRelyOnFurther();
        applicationToRelyOnFurther.setDoRequireApplicationToRely(YesOrNo.NO);
        applicationToRelyOnFurther.setApplicationToRelyOnFurtherDetails(applicationDetails);
        SdoR2QuestionsClaimantExpert questionsClaimantExpert = new SdoR2QuestionsClaimantExpert();
        questionsClaimantExpert.setSdoDefendantMayAskTxt(SdoR2UiConstantFastTrack.DEFENDANT_MAY_ASK);
        questionsClaimantExpert.setSdoDefendantMayAskDate(LocalDate.now().plusDays(126));
        questionsClaimantExpert.setSdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.QUESTIONS_SHALL_BE_ANSWERED);
        questionsClaimantExpert.setSdoQuestionsShallBeAnsweredDate(LocalDate.now().plusDays(147));
        questionsClaimantExpert.setSdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL);
        questionsClaimantExpert.setSdoApplicationToRelyOnFurther(applicationToRelyOnFurther);
        updatedData.sdoR2QuestionsClaimantExpert(questionsClaimantExpert);
        SdoR2PermissionToRelyOnExpert permissionToRelyOnExpert = new SdoR2PermissionToRelyOnExpert();
        permissionToRelyOnExpert.setSdoPermissionToRelyOnExpertTxt(SdoR2UiConstantFastTrack.PERMISSION_TO_RELY_ON_EXPERT);
        permissionToRelyOnExpert.setSdoPermissionToRelyOnExpertDate(LocalDate.now().plusDays(119));
        permissionToRelyOnExpert.setSdoJointMeetingOfExpertsTxt(SdoR2UiConstantFastTrack.JOINT_MEETING_OF_EXPERTS);
        permissionToRelyOnExpert.setSdoJointMeetingOfExpertsDate(LocalDate.now().plusDays(147));
        permissionToRelyOnExpert.setSdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL_7_DAYS);
        updatedData.sdoR2PermissionToRelyOnExpert(permissionToRelyOnExpert);
        SdoR2EvidenceAcousticEngineer acousticEngineer = new SdoR2EvidenceAcousticEngineer();
        acousticEngineer.setSdoEvidenceAcousticEngineerTxt(SdoR2UiConstantFastTrack.EVIDENCE_ACOUSTIC_ENGINEER);
        acousticEngineer.setSdoInstructionOfTheExpertTxt(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT);
        acousticEngineer.setSdoInstructionOfTheExpertDate(LocalDate.now().plusDays(42));
        acousticEngineer.setSdoInstructionOfTheExpertTxtArea(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT_TA);
        acousticEngineer.setSdoExpertReportTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT);
        acousticEngineer.setSdoExpertReportDate(LocalDate.now().plusDays(280));
        acousticEngineer.setSdoExpertReportDigitalPortalTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT_DIGITAL_PORTAL);
        acousticEngineer.setSdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS);
        acousticEngineer.setSdoWrittenQuestionsDate(LocalDate.now().plusDays(294));
        acousticEngineer.setSdoWrittenQuestionsDigitalPortalTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS_DIGITAL_PORTAL);
        acousticEngineer.setSdoRepliesTxt(SdoR2UiConstantFastTrack.REPLIES);
        acousticEngineer.setSdoRepliesDate(LocalDate.now().plusDays(315));
        acousticEngineer.setSdoRepliesDigitalPortalTxt(SdoR2UiConstantFastTrack.REPLIES_DIGITAL_PORTAL);
        acousticEngineer.setSdoServiceOfOrderTxt(SdoR2UiConstantFastTrack.SERVICE_OF_ORDER);
        updatedData.sdoR2EvidenceAcousticEngineer(acousticEngineer);
        SdoR2QuestionsToEntExpert questionsToEntExpert = new SdoR2QuestionsToEntExpert();
        questionsToEntExpert.setSdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS);
        questionsToEntExpert.setSdoWrittenQuestionsDate(LocalDate.now().plusDays(336));
        questionsToEntExpert.setSdoWrittenQuestionsDigPortalTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS_DIG_PORTAL);
        questionsToEntExpert.setSdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.ENT_QUESTIONS_SHALL_BE_ANSWERED);
        questionsToEntExpert.setSdoQuestionsShallBeAnsweredDate(LocalDate.now().plusDays(350));
        questionsToEntExpert.setSdoShallBeUploadedTxt(SdoR2UiConstantFastTrack.ENT_SHALL_BE_UPLOADED);
        updatedData.sdoR2QuestionsToEntExpert(questionsToEntExpert);
        SdoR2UploadOfDocuments uploadOfDocuments = new SdoR2UploadOfDocuments();
        uploadOfDocuments.setSdoUploadOfDocumentsTxt(SdoR2UiConstantFastTrack.UPLOAD_OF_DOCUMENTS);
        updatedData.sdoR2UploadOfDocuments(uploadOfDocuments);
        List<IncludeInOrderToggle> includeInOrderToggle = List.of(IncludeInOrderToggle.INCLUDE);
        SdoR2FastTrackAltDisputeResolution altDisputeResolution = new SdoR2FastTrackAltDisputeResolution();
        altDisputeResolution.setIncludeInOrderToggle(includeInOrderToggle);
        updatedData.sdoAltDisputeResolution(altDisputeResolution);
        SdoR2VariationOfDirections variation = new SdoR2VariationOfDirections();
        variation.setIncludeInOrderToggle(includeInOrderToggle);
        updatedData.sdoVariationOfDirections(variation);
        SdoR2Settlement settlement = new SdoR2Settlement();
        settlement.setIncludeInOrderToggle(includeInOrderToggle);
        updatedData.sdoR2Settlement(settlement);
        updatedData.sdoR2DisclosureOfDocumentsToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorWitnessesOfFactToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorExpertEvidenceToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorAddendumReportToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorFurtherAudiogramToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorQuestionsClaimantExpertToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorPermissionToRelyOnExpertToggle(includeInOrderToggle);
        updatedData.sdoR2SeparatorEvidenceAcousticEngineerToggle(includeInOrderToggle);
        updatedData.sdoR2SeparatorQuestionsToEntExpertToggle(includeInOrderToggle);
        updatedData.sdoR2ScheduleOfLossToggle(includeInOrderToggle);
        updatedData.sdoR2SeparatorUploadOfDocumentsToggle(includeInOrderToggle);
        updatedData.sdoR2TrialToggle(includeInOrderToggle);

        return updatedData.build();
    }

    @Test
    public void shouldGenerateSdoSmallDrhDocument() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_SMALL_DRH)))
            .thenReturn(new DocmosisDocument(SDO_SMALL_DRH.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameSmallDrh, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_SMALL_DRH);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .build()
            .toBuilder()
            .claimsTrack(ClaimsTrack.smallClaimsTrack)
            .drawDirectionsOrderRequired(NO)
            .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
            .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameSmallDrh, bytes, SDO_ORDER));
    }

    @Test
    public void shouldGenerateSdoSmallDrhDocumentCarmEnabled() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_SMALL_DRH)))
            .thenReturn(new DocmosisDocument(SDO_SMALL_DRH.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameSmallDrh, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_SMALL_DRH);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .build()
            .toBuilder()
            .claimsTrack(ClaimsTrack.smallClaimsTrack)
            .drawDirectionsOrderRequired(NO)
            .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
            .sdoR2SmallClaimsMediationSectionStatement(buildMediation("mediation representation"))
            .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameSmallDrh, bytes, SDO_ORDER));
        verify(documentGeneratorService).generateDocmosisDocument(
            argThat((MappableObject templateData) ->
                        templateData instanceof SdoDocumentFormSmallDrh
                            && ((SdoDocumentFormSmallDrh) templateData).getSdoR2SmallClaimMediationSectionInput()
                            .equals("mediation representation")),
            any(DocmosisTemplates.class)
        );
    }

    private SdoR2SmallClaimsMediation buildMediation(String text) {
        SdoR2SmallClaimsMediation mediation = new SdoR2SmallClaimsMediation();
        mediation.setInput(text);
        return mediation;
    }

}
