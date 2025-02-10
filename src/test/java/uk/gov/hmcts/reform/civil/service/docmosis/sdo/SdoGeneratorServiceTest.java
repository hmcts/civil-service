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
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
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
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
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
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsFlightDelay;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.utils.HearingMethodUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_FAST;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_FAST_FAST_TRACK_INT_R2;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_FAST_R2;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_FAST_TRACK_NIHL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_R2_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_SMALL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_SMALL_DRH;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_SMALL_R2;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    SdoGeneratorService.class,
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
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_SMALL)))
            .thenReturn(new DocmosisDocument(SDO_SMALL.getDocumentTitle(), bytes));
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
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_SMALL)))
            .thenReturn(new DocmosisDocument(SDO_SMALL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameSmall, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_SMALL);

        LocationRefData locationRefData = LocationRefData.builder().build();
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
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        LocationRefData locationRefData = LocationRefData.builder().build();
        String locationLabel = "String 1";
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.smallClaimsTrack)
            .smallClaims(List.of(SmallTrack.smallClaimFlightDelay))
            .smallClaimsFlightDelayToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .smallClaimsFlightDelay(SmallClaimsFlightDelay.builder()
                                        .relatedClaimsInput("Test Data 1")
                                        .legalDocumentsInput("Test data 2")
                                        .build())
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
        assertThat(argument.getValue().getSmallClaimsFlightDelay().getRelatedClaimsInput()).isEqualTo("Test Data 1");
        assertThat(argument.getValue().getSmallClaimsFlightDelay().getLegalDocumentsInput()).isEqualTo("Test data 2");
    }

    @Test
    public void sdoSmallInPersonCarm() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_SMALL)))
            .thenReturn(new DocmosisDocument(SDO_SMALL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameSmall, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_SMALL);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        LocationRefData locationRefData = LocationRefData.builder().build();
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
            .smallClaimsMediationSectionStatement(SmallClaimsMediation.builder()
                                                      .input("mediation representation")
                                                      .build())
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
    public void shouldGenerateSdoFastTrackDocument() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_FAST)))
            .thenReturn(new DocmosisDocument(SDO_FAST.getDocumentTitle(), bytes));
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

        //assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER));
    }

    @Test
    public void shouldGenerateSdoFastTrackDocumentInPerson() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_FAST)))
            .thenReturn(new DocmosisDocument(SDO_FAST.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_FAST);

        LocationRefData locationRefData = LocationRefData.builder().build();
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
            .atStateSdoFastTrackTrial()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson)
            .fastTrackMethodInPerson(formValue)
            .build();
        when(documentHearingLocationHelper.getHearingLocation(locationLabel, caseData, BEARER_TOKEN))
            .thenReturn(locationRefData);

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER));
        verify(documentGeneratorService).generateDocmosisDocument(
            argThat((MappableObject templateData) ->
                        templateData instanceof SdoDocumentFormFast
                            && locationRefData.equals(((SdoDocumentFormFast) templateData).getHearingLocation())),
            any(DocmosisTemplates.class)
        );
    }

    @Test
    public void shouldGenerateSdoFastTrackDocumentWithR2Template() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_FAST_R2)))
            .thenReturn(new DocmosisDocument(SDO_FAST_R2.getDocumentTitle(), bytes));
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
            eq(SDO_FAST_R2)
        );
    }

    @Test
    public void shouldGenerateSdoFastTrackDocumentWithR2TemplateAndFastTrackUpLiftsEnabled() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        when(featureToggleService.isFastTrackUpliftsEnabled()).thenReturn(true);

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
    public void shouldGenerateSdoR2FastTrackDocumentWithR2TemplateAndFastTrackUpLiftsEnabled() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        when(featureToggleService.isFastTrackUpliftsEnabled()).thenReturn(true);

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_FAST_FAST_TRACK_INT_R2)))
            .thenReturn(new DocmosisDocument(SDO_FAST_FAST_TRACK_INT_R2.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_FAST);
        List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
        fastTrackList.add(FastTrack.fastClaimCreditHire);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateSdoFastTrackCreditHire()
            .build()
            .toBuilder()
            .trialAdditionalDirectionsForFastTrack(fastTrackList)
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
    public void shouldGenerateSdoDisposalDocument() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_DISPOSAL)))
            .thenReturn(new DocmosisDocument(SDO_DISPOSAL.getDocumentTitle(), bytes));
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

        LocationRefData locationRefData = LocationRefData.builder().build();
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
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_DISPOSAL)))
            .thenReturn(new DocmosisDocument(SDO_DISPOSAL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameDisposal, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_DISPOSAL);

        LocationRefData locationRefData = LocationRefData.builder().build();
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
    public void shouldGenerateSdoDisposalDocumentWithR2Template() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_R2_DISPOSAL)))
            .thenReturn(new DocmosisDocument(SDO_R2_DISPOSAL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameDisposal, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_FAST);

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
            .disposalHearingHearingTime(DisposalHearingHearingTime.builder().time(
                DisposalHearingFinalDisposalHearingTimeEstimate.OTHER).otherHours("2").otherMinutes("30").build())
            .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameDisposal, bytes, SDO_ORDER));
        verify(documentGeneratorService).generateDocmosisDocument(
            argThat((MappableObject templateData) ->
                        templateData instanceof SdoDocumentFormDisposal),
            eq(SDO_R2_DISPOSAL)
        );
    }

    @Test
    public void shouldGenerateSdoFastTrackNihlDocument_pathone() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_FAST_TRACK_NIHL)))
            .thenReturn(new DocmosisDocument(SDO_FAST_TRACK_NIHL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_FAST);
        List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
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
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_FAST_TRACK_NIHL)))
            .thenReturn(new DocmosisDocument(SDO_FAST_TRACK_NIHL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_FAST);
        List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
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

        Category inPerson = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag("Y").build();
        Category video = Category.builder().categoryKey("HearingChannel").key("VID").valueEn("Video").activeFlag("Y").build();
        Category telephone = Category.builder().categoryKey("HearingChannel").key("TEL").valueEn("Telephone").activeFlag("Y").build();
        CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(inPerson, video, telephone)).build();
        when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), eq("HearingChannel"), anyString())).thenReturn(
            Optional.of(categorySearchResult));

        List<IncludeInOrderToggle> includeInOrderToggle = List.of(IncludeInOrderToggle.INCLUDE);
        DynamicListElement selectedCourt = DynamicListElement.builder()
            .code("00002").label("court 2 - 2 address - Y02 7RB").build();
        updatedData.sdoFastTrackJudgesRecital(FastTrackJudgesRecital.builder()
                                                  .input(SdoR2UiConstantFastTrack.JUDGE_RECITAL).build());
        updatedData.sdoR2DisclosureOfDocuments(SdoR2DisclosureOfDocuments.builder()
                                                   .standardDisclosureTxt(SdoR2UiConstantFastTrack.STANDARD_DISCLOSURE)
                                                   .standardDisclosureDate(LocalDate.now().plusDays(28))
                                                   .inspectionTxt(SdoR2UiConstantFastTrack.INSPECTION)
                                                   .inspectionDate(LocalDate.now().plusDays(42))
                                                   .requestsWillBeCompiledLabel(SdoR2UiConstantFastTrack.REQUEST_COMPILED_WITH)
                                                   .build());
        updatedData.sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                                             .sdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS)
                                             .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                                                       .isRestrictWitness(NO)
                                                                       .restrictNoOfWitnessDetails(
                                                                           SdoR2RestrictNoOfWitnessDetails
                                                                               .builder()
                                                                               .noOfWitnessClaimant(3).noOfWitnessDefendant(3)
                                                                               .partyIsCountedAsWitnessTxt(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT)
                                                                               .build())
                                                                       .build())
                                             .sdoRestrictPages(SdoR2RestrictPages.builder()
                                                                   .isRestrictPages(NO)
                                                                   .restrictNoOfPagesDetails(
                                                                       SdoR2RestrictNoOfPagesDetails.builder()
                                                                           .witnessShouldNotMoreThanTxt(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1)
                                                                           .noOfPages(12)
                                                                           .fontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2)
                                                                           .build()).build())
                                             .sdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE)
                                             .sdoWitnessDeadlineDate(LocalDate.now().plusDays(70))
                                             .sdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE)
                                             .build());
        updatedData.sdoR2ScheduleOfLoss(SdoR2ScheduleOfLoss.builder().sdoR2ScheduleOfLossClaimantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT)
                                            .isClaimForPecuniaryLoss(NO)
                                            .sdoR2ScheduleOfLossClaimantDate(LocalDate.now().plusDays(364))
                                            .sdoR2ScheduleOfLossDefendantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT)
                                            .sdoR2ScheduleOfLossDefendantDate(LocalDate.now().plusDays(378))
                                            .sdoR2ScheduleOfLossPecuniaryLossTxt(SdoR2UiConstantFastTrack.PECUNIARY_LOSS)
                                            .build());
        DynamicList options = DynamicList.builder()
            .listItems(List.of(
                           DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                           DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                           DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                       )
            ).build();
        updatedData.sdoR2Trial(SdoR2Trial.builder()
                                   .trialOnOptions(TrialOnRadioOptions.OPEN_DATE)
                                   .lengthList(FastTrackHearingTimeEstimate.FIVE_HOURS)
                                   .methodOfHearing(HearingMethodUtils.getHearingMethodList(categorySearchResult))
                                   .physicalBundleOptions(PhysicalTrialBundleOptions.NONE)
                                   .sdoR2TrialFirstOpenDateAfter(
                                       SdoR2TrialFirstOpenDateAfter.builder()
                                           .listFrom(LocalDate.now().plusDays(434)).build())
                                   .sdoR2TrialWindow(SdoR2TrialWindow.builder()
                                                         .listFrom(LocalDate.now().plusDays(434))
                                                         .dateTo(LocalDate.now().plusDays(455))
                                                         .build())
                                   .hearingCourtLocationList((options.toBuilder().value(selectedCourt).build()))
                                   .physicalBundlePartyTxt(SdoR2UiConstantFastTrack.PHYSICAL_TRIAL_BUNDLE)
                                   .build());

        updatedData.sdoR2ImportantNotesTxt(SdoR2UiConstantFastTrack.IMPORTANT_NOTES);
        updatedData.sdoR2ImportantNotesDate(LocalDate.now().plusDays(7));

        updatedData.sdoR2ExpertEvidence(SdoR2ExpertEvidence.builder()
                                            .sdoClaimantPermissionToRelyTxt(SdoR2UiConstantFastTrack.CLAIMANT_PERMISSION_TO_RELY).build());
        updatedData.sdoR2AddendumReport(SdoR2AddendumReport.builder()
                                            .sdoAddendumReportTxt(SdoR2UiConstantFastTrack.ADDENDUM_REPORT)
                                            .sdoAddendumReportDate(LocalDate.now().plusDays(56)).build());
        updatedData.sdoR2FurtherAudiogram(SdoR2FurtherAudiogram.builder()
                                              .sdoClaimantShallUndergoTxt(SdoR2UiConstantFastTrack.CLAIMANT_SHALL_UNDERGO)
                                              .sdoServiceReportTxt(SdoR2UiConstantFastTrack.SERVICE_REPORT)
                                              .sdoClaimantShallUndergoDate(LocalDate.now().plusDays(42))
                                              .sdoServiceReportDate(LocalDate.now().plusDays(98)).build());
        updatedData.sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
                                                     .sdoDefendantMayAskTxt(SdoR2UiConstantFastTrack.DEFENDANT_MAY_ASK)
                                                     .sdoDefendantMayAskDate(LocalDate.now().plusDays(126))
                                                     .sdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.QUESTIONS_SHALL_BE_ANSWERED)
                                                     .sdoQuestionsShallBeAnsweredDate(LocalDate.now().plusDays(147))
                                                     .sdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL)
                                                     .sdoApplicationToRelyOnFurther(
                                                         SdoR2ApplicationToRelyOnFurther.builder()
                                                             .doRequireApplicationToRely(YesOrNo.NO)
                                                             .applicationToRelyOnFurtherDetails(
                                                                 SdoR2ApplicationToRelyOnFurtherDetails.builder()
                                                                     .applicationToRelyDetailsTxt(SdoR2UiConstantFastTrack.APPLICATION_TO_RELY_DETAILS)
                                                                     .applicationToRelyDetailsDate(LocalDate.now().plusDays(161)).build()).build())
                                                     .build());
        updatedData.sdoR2PermissionToRelyOnExpert(SdoR2PermissionToRelyOnExpert.builder()
                                                      .sdoPermissionToRelyOnExpertTxt(SdoR2UiConstantFastTrack.PERMISSION_TO_RELY_ON_EXPERT)
                                                      .sdoPermissionToRelyOnExpertDate(LocalDate.now().plusDays(119))
                                                      .sdoJointMeetingOfExpertsTxt(SdoR2UiConstantFastTrack.JOINT_MEETING_OF_EXPERTS)
                                                      .sdoJointMeetingOfExpertsDate(LocalDate.now().plusDays(147))
                                                      .sdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL_7_DAYS)
                                                      .build());
        updatedData.sdoR2EvidenceAcousticEngineer(SdoR2EvidenceAcousticEngineer.builder()
                                                      .sdoEvidenceAcousticEngineerTxt(SdoR2UiConstantFastTrack.EVIDENCE_ACOUSTIC_ENGINEER)
                                                      .sdoInstructionOfTheExpertTxt(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT)
                                                      .sdoInstructionOfTheExpertDate(LocalDate.now().plusDays(42))
                                                      .sdoInstructionOfTheExpertTxtArea(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT_TA)
                                                      .sdoExpertReportTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT)
                                                      .sdoExpertReportDate(LocalDate.now().plusDays(280))
                                                      .sdoExpertReportDigitalPortalTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT_DIGITAL_PORTAL)
                                                      .sdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS)
                                                      .sdoWrittenQuestionsDate(LocalDate.now().plusDays(294))
                                                      .sdoWrittenQuestionsDigitalPortalTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS_DIGITAL_PORTAL)
                                                      .sdoRepliesTxt(SdoR2UiConstantFastTrack.REPLIES)
                                                      .sdoRepliesDate(LocalDate.now().plusDays(315))
                                                      .sdoRepliesDigitalPortalTxt(SdoR2UiConstantFastTrack.REPLIES_DIGITAL_PORTAL)
                                                      .sdoServiceOfOrderTxt(SdoR2UiConstantFastTrack.SERVICE_OF_ORDER)
                                                      .build());
        updatedData.sdoR2QuestionsToEntExpert(SdoR2QuestionsToEntExpert.builder()
                                                  .sdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS)
                                                  .sdoWrittenQuestionsDate(LocalDate.now().plusDays(336))
                                                  .sdoWrittenQuestionsDigPortalTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS_DIG_PORTAL)
                                                  .sdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.ENT_QUESTIONS_SHALL_BE_ANSWERED)
                                                  .sdoQuestionsShallBeAnsweredDate(LocalDate.now().plusDays(350))
                                                  .sdoShallBeUploadedTxt(SdoR2UiConstantFastTrack.ENT_SHALL_BE_UPLOADED)
                                                  .build());
        updatedData.sdoR2UploadOfDocuments(SdoR2UploadOfDocuments.builder()
                                               .sdoUploadOfDocumentsTxt(SdoR2UiConstantFastTrack.UPLOAD_OF_DOCUMENTS)
                                               .build());
        updatedData.sdoAltDisputeResolution(SdoR2FastTrackAltDisputeResolution.builder().includeInOrderToggle(includeInOrderToggle).build());
        updatedData.sdoVariationOfDirections(SdoR2VariationOfDirections.builder().includeInOrderToggle(includeInOrderToggle).build());
        updatedData.sdoR2Settlement(SdoR2Settlement.builder().includeInOrderToggle(includeInOrderToggle).build());
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
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
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
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
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
            .sdoR2SmallClaimsMediationSectionStatement(SdoR2SmallClaimsMediation.builder()
                                                      .input("mediation representation")
                                                      .build())
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

}
