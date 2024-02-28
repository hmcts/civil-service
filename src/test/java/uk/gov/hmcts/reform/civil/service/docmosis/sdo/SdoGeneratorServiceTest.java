package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormDisposal;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFast;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmall;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsFlightDelay;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_FAST;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_SMALL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_SMALL_FLIGHT_DELAY;

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

    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    protected IdamClient idamClient;

    @MockBean
    private DocumentHearingLocationHelper documentHearingLocationHelper;

    @Autowired
    private SdoGeneratorService generator;

    @BeforeEach
    void setUp() {
        fileNameDisposal = LocalDate.now() + "_Judgey McJudge" + ".pdf";
        fileNameFast = LocalDate.now() + "_Judgey McJudge" + ".pdf";
        fileNameSmall = LocalDate.now() + "_Judgey McJudge" + ".pdf";

        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
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
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_SMALL_FLIGHT_DELAY)))
            .thenReturn(new DocmosisDocument(SDO_SMALL_FLIGHT_DELAY.getDocumentTitle(), bytes));
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
}
