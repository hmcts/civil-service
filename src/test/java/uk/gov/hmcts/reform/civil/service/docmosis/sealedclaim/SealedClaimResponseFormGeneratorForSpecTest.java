package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class SealedClaimResponseFormGeneratorForSpecTest {

    private static final String BEARER_TOKEN = "Bearer Token";
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

    @Test
    public void contentCheck() {
        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("SiteName").courtAddress("1").postcode("1")
                          .courtName("Court Name").region("Region").regionId("4").courtVenueId("000")
                          .courtTypeId("10").courtLocationCode("121")
                          .epimmsId("000000").build());
        when(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).thenReturn(locations);

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
    public void contentCheckMultiparty() {
        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("SiteName").courtAddress("1").postcode("1")
                          .courtName("Court Name2").region("Region").regionId("4").courtVenueId("000")
                          .courtTypeId("10").courtLocationCode("121")
                          .epimmsId("000000").build());
        when(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).thenReturn(locations);

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
            caseData,BEARER_TOKEN);

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
}
