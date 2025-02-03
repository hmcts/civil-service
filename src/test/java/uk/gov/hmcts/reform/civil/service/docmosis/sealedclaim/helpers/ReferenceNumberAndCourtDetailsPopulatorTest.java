package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class ReferenceNumberAndCourtDetailsPopulatorTest {

    @InjectMocks
    private ReferenceNumberAndCourtDetailsPopulator referenceNumberPopulator;

    @Mock
    private LocationReferenceDataService locationRefDataService;

    private static final List<LocationRefData> LOCATIONS = List.of(LocationRefData.builder().siteName("SiteName").courtAddress("1").postcode("1")
                                                                       .courtName("Court Name").region("Region").regionId("4").courtVenueId("000")
                                                                       .courtTypeId("10").courtLocationCode("121")
                                                                       .epimmsId("000000").build());

    @Test
    void testPopulateDetails_Respondent1() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQRequestedCourt(
                RequestedCourt.builder()
                    .responseCourtCode("121")
                    .reasonForHearingAtSpecificCourt("test")
                    .caseLocation(CaseLocationCivil.builder()
                                      .region("2")
                                      .baseLocation("000000")
                                      .build())
                    .build())
            .build();
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Applicant Name")
                            .build())
            .respondent1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                             .partyName("Respondent Name")
                            .build())
            .respondent1DQ(respondent1DQ)
            .legacyCaseReference("12345")
            .detailsOfWhyDoesYouDisputeTheClaim("Dispute details")
            .build();

        given(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).willReturn(LOCATIONS);

        SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder = SealedClaimResponseFormForSpec.builder();
        referenceNumberPopulator.populateReferenceNumberDetails(builder, caseData, "authorisation");

        SealedClaimResponseFormForSpec form = builder.build();
        assertEquals("12345", form.getReferenceNumber());
        assertEquals("Dispute details", form.getWhyDisputeTheClaim());
        assertEquals("Court Name", form.getHearingCourtLocation());
    }

    @Test
    void testPopulateDetails_Respondent2() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQRequestedCourt(
                RequestedCourt.builder()
                    .responseCourtCode("121")
                    .reasonForHearingAtSpecificCourt("test")
                    .caseLocation(CaseLocationCivil.builder()
                                      .region("2")
                                      .baseLocation("000000")
                                      .build())
                    .build())
            .build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQRequestedCourt(
                RequestedCourt.builder()
                    .responseCourtCode("121")
                    .reasonForHearingAtSpecificCourt("test")
                    .caseLocation(CaseLocationCivil.builder()
                                      .region("2")
                                      .baseLocation("000000")
                                      .build())
                    .build())
            .build();
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Applicant Name")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Respondent One")
                             .build())
            .respondent2(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Respondent Two")
                             .build())
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .legacyCaseReference("12345")
            .detailsOfWhyDoesYouDisputeTheClaim("Dispute details")
            .build();

        given(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).willReturn(LOCATIONS);

        SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder = SealedClaimResponseFormForSpec.builder();
        referenceNumberPopulator.populateReferenceNumberDetails(builder, caseData, "authorisation");

        SealedClaimResponseFormForSpec form = builder.build();
        assertEquals("12345", form.getReferenceNumber());
        assertEquals("Dispute details", form.getWhyDisputeTheClaim());
        assertEquals("Court Name", form.getHearingCourtLocation());
    }

    @Test
    void testPopulateDetails_NoCourtLocation() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQRequestedCourt(
                RequestedCourt.builder()
                    .responseCourtCode("121")
                    .reasonForHearingAtSpecificCourt("test")
                    .caseLocation(CaseLocationCivil.builder()
                                      .region("2")
                                      .baseLocation("000000")
                                      .build())
                    .build())
            .build();
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Applicant Name")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Respondent One")
                             .build())
            .respondent1DQ(respondent1DQ)
            .legacyCaseReference("12345")
            .detailsOfWhyDoesYouDisputeTheClaim("Dispute details")
            .build();

        given(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).willReturn(Collections.emptyList());

        SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder = SealedClaimResponseFormForSpec.builder();
        referenceNumberPopulator.populateReferenceNumberDetails(builder, caseData, "authorisation");

        SealedClaimResponseFormForSpec form = builder.build();
        assertNull(form.getHearingCourtLocation());
    }

    @Test
    void testPopulateDetails_NeitherRespondentCourt() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQRequestedCourt(
                RequestedCourt.builder()
                    .responseCourtCode("121")
                    .reasonForHearingAtSpecificCourt("test")
                    .caseLocation(CaseLocationCivil.builder()
                                      .region("2")
                                      .baseLocation("000000")
                                      .build())
                    .build())
            .build();
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Applicant Name")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Respondent One")
                             .build())
            .respondent1DQ(respondent1DQ)
            .legacyCaseReference("12345")
            .detailsOfWhyDoesYouDisputeTheClaim("Dispute details")
            .build();

        SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder = SealedClaimResponseFormForSpec.builder();
        referenceNumberPopulator.populateReferenceNumberDetails(builder, caseData, "authorisation");

        SealedClaimResponseFormForSpec form = builder.build();
        assertEquals("12345", form.getReferenceNumber());
        assertNull(form.getHearingCourtLocation());
    }
}
