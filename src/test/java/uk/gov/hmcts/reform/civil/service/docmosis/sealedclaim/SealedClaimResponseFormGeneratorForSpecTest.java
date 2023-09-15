package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentMethod;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class SealedClaimResponseFormGeneratorForSpecTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final List<LocationRefData> LOCATIONS = List.of(LocationRefData.builder().siteName("SiteName").courtAddress("1").postcode("1")
                                                                       .courtName("Court Name").region("Region").regionId("4").courtVenueId("000")
                                                                       .courtTypeId("10").courtLocationCode("121")
                                                                       .epimmsId("000000").build());
    private static final CaseData CASE_DATA_WITH_RESPONDENT1 = getCaseDataWithRespondent1Data();

    @InjectMocks
    private SealedClaimResponseFormGeneratorForSpec generator;

    @Mock
    private RepresentativeService representativeService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private DocumentManagementService documentManagementService;
    @MockBean
    private CourtLocationUtils courtLocationUtils;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Captor
    private ArgumentCaptor<DocmosisTemplates> docmosisTemplatesArgumentCaptor;
    @Captor
    private ArgumentCaptor<SealedClaimResponseFormForSpec> templateDataCaptor;

    @BeforeEach
    void setUp() {
        given(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).willReturn(LOCATIONS);
    }

    @Test
    void contentCheckRespondent1() {
        SealedClaimResponseFormForSpec templateData = generator.getTemplateData(
            CASE_DATA_WITH_RESPONDENT1, BEARER_TOKEN);

        Assertions.assertEquals(CASE_DATA_WITH_RESPONDENT1.getLegacyCaseReference(), templateData.getReferenceNumber());
        Assertions.assertEquals(
            CASE_DATA_WITH_RESPONDENT1.getDetailsOfWhyDoesYouDisputeTheClaim(),
            templateData.getWhyDisputeTheClaim()
        );
        Assertions.assertEquals(
            CASE_DATA_WITH_RESPONDENT1.getRespondent1DQ().getRespondent1DQStatementOfTruth().getName(),
            templateData.getStatementOfTruth().getName()
        );
        Assertions.assertEquals(
            CASE_DATA_WITH_RESPONDENT1.getRespondent1DQ().getRespondent1DQStatementOfTruth().getRole(),
            templateData.getStatementOfTruth().getRole()
        );
    }

    @Test
    public void contentCheckRespondent2() {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("case reference")
            .detailsOfWhyDoesYouDisputeTheClaim("why dispute the claim")
            .respondent1DQ(Respondent1DQ.builder().respondent1DQStatementOfTruth(
                StatementOfTruth.builder()
                    .name("sot1 name")
                    .role("sot1 role")
                    .build()).build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQStatementOfTruth(
                                   StatementOfTruth.builder()
                                       .name("sot2 name")
                                       .role("sot2 role")
                                       .build()
                               )
                               .respondent2DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .responseCourtCode("121")
                                       .caseLocation(CaseLocationCivil.builder()
                                                         .region("2")
                                                         .baseLocation("000000")
                                                         .build())
                                       .build())
                               .build())
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("applicant name")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("defendant name")
                             .build())
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("defendant2 name")
                             .build())
            .respondent2ResponseDate(LocalDateTime.now())
            .build();

        SealedClaimResponseFormForSpec templateData = generator.getTemplateData(
            caseData, BEARER_TOKEN);

        Assertions.assertEquals(caseData.getLegacyCaseReference(), templateData.getReferenceNumber());
        Assertions.assertEquals(caseData.getDetailsOfWhyDoesYouDisputeTheClaim(),
                                templateData.getWhyDisputeTheClaim());
        Assertions.assertEquals(caseData.getRespondent2DQ().getRespondent2DQStatementOfTruth().getName(),
                                templateData.getStatementOfTruth().getName());
        Assertions.assertEquals(caseData.getRespondent2DQ().getRespondent2DQStatementOfTruth().getRole(),
                                templateData.getStatementOfTruth().getRole());
        Assertions.assertEquals(LOCATIONS.get(0).getCourtName(),
                                templateData.getHearingCourtLocation());
    }

    @Test
    public void contentCheckMultiparty() {
        List<TimelineOfEvents> timelines = new ArrayList<>();
        timelines.add(TimelineOfEvents.builder()
                          .value(TimelineOfEventDetails.builder()
                                     .timelineDate(LocalDate.now()).timelineDescription("test timeline").build()).build());
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("case reference")
            .detailsOfWhyDoesYouDisputeTheClaim("why dispute the claim")
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQStatementOfTruth(
                                   StatementOfTruth.builder()
                                       .name("sot name")
                                       .role("sot role")
                                       .build()
                               )
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .responseCourtCode("121")
                                       .caseLocation(CaseLocationCivil.builder()
                                                         .region("2")
                                                         .baseLocation("000000")
                                                         .build())
                                       .build())
                               .build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQStatementOfTruth(
                                   StatementOfTruth.builder()
                                       .name("sot2 name")
                                       .role("sot2 role")
                                       .build()
                               )
                               .respondent2DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .responseCourtCode("121")
                                       .caseLocation(CaseLocationCivil.builder()
                                                         .region("2")
                                                         .baseLocation("000000")
                                                         .build())
                                       .build())
                               .build())
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("applicant name")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("defendant name")
                             .build())
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("defendant2 name")
                             .build())
            .respondent2Copy(Party.builder()
                                 .type(Party.Type.COMPANY)
                                 .companyName("defendant2 name")
                                 .build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent2ResponseDate(LocalDateTime.now().plusDays(3))
            .respondToAdmittedClaim(RespondToClaim.builder()
                                        .howMuchWasPaid(new BigDecimal(1000))
                                        .howWasThisAmountPaid(PaymentMethod.CREDIT_CARD)
                                        .whenWasThisAmountPaid(LocalDate.now()).build())
            .specResponseTimelineOfEvents(timelines)
            .build();
        SealedClaimResponseFormForSpec templateData = generator.getTemplateData(
            caseData, BEARER_TOKEN);

        Assertions.assertEquals(caseData.getLegacyCaseReference(), templateData.getReferenceNumber());
        Assertions.assertEquals(
            caseData.getDetailsOfWhyDoesYouDisputeTheClaim(),
            templateData.getWhyDisputeTheClaim()
        );
        Assertions.assertEquals(
            caseData.getRespondent1DQ().getRespondent1DQStatementOfTruth().getName(),
            templateData.getStatementOfTruth().getName()
        );
        Assertions.assertEquals(
            caseData.getRespondent1DQ().getRespondent1DQStatementOfTruth().getRole(),
            templateData.getStatementOfTruth().getRole()
        );
    }

    @Test
    void shouldSelectTemplateWithRepaymentPlan_whenPinAndPostEnabled() {
        //Given
        DocmosisDocument docmosisDocument = DocmosisDocument.builder().build();
        given(featureToggleService.isPinInPostEnabled()).willReturn(true);
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
            .willReturn(docmosisDocument);
        //When
        generator.generate(CASE_DATA_WITH_RESPONDENT1, BEARER_TOKEN);
        //Then
        verify(documentGeneratorService).generateDocmosisDocument(templateDataCaptor.capture(), docmosisTemplatesArgumentCaptor.capture());
        assertThat(docmosisTemplatesArgumentCaptor.getValue()).isEqualTo(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_SEALED_1V1_INSTALLMENTS);
    }

    @Test
    void shouldSelectTemplateWithoutRepaymentPlan_whenPinAndPostDisabled() {
        //Given
        DocmosisDocument docmosisDocument = DocmosisDocument.builder().build();
        given(featureToggleService.isPinInPostEnabled()).willReturn(false);
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
            .willReturn(docmosisDocument);
        //When
        generator.generate(CASE_DATA_WITH_RESPONDENT1, BEARER_TOKEN);
        //Then
        verify(documentGeneratorService).generateDocmosisDocument(templateDataCaptor.capture(), docmosisTemplatesArgumentCaptor.capture());
        assertThat(docmosisTemplatesArgumentCaptor.getValue()).isEqualTo(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_SEALED_1v1);
    }

    @Test
    void shouldSelectMultipartyTemplate_whenMultipartyCase() {
        //Given
        DocmosisDocument docmosisDocument = DocmosisDocument.builder().build();
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
            .willReturn(docmosisDocument);
        CaseData multipartyCaseData = CASE_DATA_WITH_RESPONDENT1.toBuilder()
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("defendant2 name")
                             .build())
            .respondentResponseIsSame(YesOrNo.YES)
            .build();
        //When
        generator.generate(multipartyCaseData, BEARER_TOKEN);
        //Then
        verify(documentGeneratorService).generateDocmosisDocument(templateDataCaptor.capture(), docmosisTemplatesArgumentCaptor.capture());
        assertThat(docmosisTemplatesArgumentCaptor.getValue()).isEqualTo(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_SEALED_1v2);
    }

    private static CaseData getCaseDataWithRespondent1Data() {
        return CaseData.builder()
            .legacyCaseReference("case reference")
            .detailsOfWhyDoesYouDisputeTheClaim("why dispute the claim")
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQStatementOfTruth(
                                   StatementOfTruth.builder()
                                       .name("sot name")
                                       .role("sot role")
                                       .build()
                               )
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .responseCourtCode("121")
                                       .reasonForHearingAtSpecificCourt("test")
                                       .caseLocation(CaseLocationCivil.builder()
                                                         .region("2")
                                                         .baseLocation("000000")
                                                         .build())
                                       .build())
                               .build())
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("applicant name")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("defendant name")
                             .build())
            .respondent1ResponseDate(LocalDateTime.now())
            .build();
    }
}
