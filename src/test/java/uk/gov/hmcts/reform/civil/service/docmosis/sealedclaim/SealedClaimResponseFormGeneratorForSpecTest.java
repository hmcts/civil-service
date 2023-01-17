package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocation;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
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
    public void contentCheckRespondent1() {
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
                                       .caseLocation(CaseLocation.builder()
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
            caseData);

        Assert.assertEquals(caseData.getLegacyCaseReference(), templateData.getReferenceNumber());
        Assert.assertEquals(caseData.getDetailsOfWhyDoesYouDisputeTheClaim(),
                            templateData.getWhyDisputeTheClaim());
        Assert.assertEquals(caseData.getRespondent1DQ().getRespondent1DQStatementOfTruth().getName(),
                            templateData.getStatementOfTruth().getName());
        Assert.assertEquals(caseData.getRespondent1DQ().getRespondent1DQStatementOfTruth().getRole(),
                            templateData.getStatementOfTruth().getRole());
        Assert.assertEquals(locations.get(0).getCourtName(),
                            templateData.getHearingCourtLocation());
    }

    @Test
    public void contentCheckRespondent2() {
        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("SiteName").courtAddress("1").postcode("1")
                          .courtName("Court Name2").region("Region").regionId("4").courtVenueId("000")
                          .courtTypeId("10").courtLocationCode("121")
                          .epimmsId("000000").build());
        when(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).thenReturn(locations);
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
                                       .caseLocation(CaseLocation.builder()
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
            caseData);

        Assert.assertEquals(caseData.getLegacyCaseReference(), templateData.getReferenceNumber());
        Assert.assertEquals(caseData.getDetailsOfWhyDoesYouDisputeTheClaim(),
                            templateData.getWhyDisputeTheClaim());
        Assert.assertEquals(caseData.getRespondent2DQ().getRespondent2DQStatementOfTruth().getName(),
                            templateData.getStatementOfTruth().getName());
        Assert.assertEquals(caseData.getRespondent2DQ().getRespondent2DQStatementOfTruth().getRole(),
                            templateData.getStatementOfTruth().getRole());
        Assert.assertEquals(locations.get(0).getCourtName(),
                            templateData.getHearingCourtLocation());
    }
}
