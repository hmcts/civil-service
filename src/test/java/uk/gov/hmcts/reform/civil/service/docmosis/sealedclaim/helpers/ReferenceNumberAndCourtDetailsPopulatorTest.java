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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
        CaseLocationCivil caseLocation = new CaseLocationCivil();
        caseLocation.setRegion("2");
        caseLocation.setBaseLocation("000000");
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setResponseCourtCode("121");
        requestedCourt.setReasonForHearingAtSpecificCourt("test");
        requestedCourt.setCaseLocation(caseLocation);
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQRequestedCourt(requestedCourt);
        
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.INDIVIDUAL);
        applicant1.setPartyName("Applicant Name");
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setPartyName("Respondent Name");
        
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .respondent1DQ(respondent1DQ)
            .legacyCaseReference("12345")
            .ccdCaseReference(1234567890123456L)
            .build();
        caseData.setDetailsOfWhyDoesYouDisputeTheClaim("Dispute details");

        given(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).willReturn(LOCATIONS);

        SealedClaimResponseFormForSpec form = new SealedClaimResponseFormForSpec();
        referenceNumberPopulator.populateReferenceNumberDetails(form, caseData, "authorisation");
        assertEquals("12345", form.getReferenceNumber());
        assertEquals("1234567890123456", form.getCcdCaseReference());
        assertEquals("Dispute details", form.getWhyDisputeTheClaim());
        assertEquals("Court Name", form.getHearingCourtLocation());
    }

    @Test
    void testPopulateDetails_Respondent2() {
        CaseLocationCivil caseLocation1 = new CaseLocationCivil();
        caseLocation1.setRegion("2");
        caseLocation1.setBaseLocation("000000");
        RequestedCourt requestedCourt1 = new RequestedCourt();
        requestedCourt1.setResponseCourtCode("121");
        requestedCourt1.setReasonForHearingAtSpecificCourt("test");
        requestedCourt1.setCaseLocation(caseLocation1);
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQRequestedCourt(requestedCourt1);
        
        CaseLocationCivil caseLocation2 = new CaseLocationCivil();
        caseLocation2.setRegion("2");
        caseLocation2.setBaseLocation("000000");
        RequestedCourt requestedCourt2 = new RequestedCourt();
        requestedCourt2.setResponseCourtCode("121");
        requestedCourt2.setReasonForHearingAtSpecificCourt("test");
        requestedCourt2.setCaseLocation(caseLocation2);
        Respondent2DQ respondent2DQ = new Respondent2DQ();
        respondent2DQ.setRespondent2DQRequestedCourt(requestedCourt2);
        
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.INDIVIDUAL);
        applicant1.setPartyName("Applicant Name");
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setPartyName("Respondent One");
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.INDIVIDUAL);
        respondent2.setPartyName("Respondent Two");
        
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .respondent2(respondent2)
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .legacyCaseReference("12345")
            .build();
        caseData.setDetailsOfWhyDoesYouDisputeTheClaim("Dispute details");

        given(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).willReturn(LOCATIONS);

        SealedClaimResponseFormForSpec form = new SealedClaimResponseFormForSpec();
        referenceNumberPopulator.populateReferenceNumberDetails(form, caseData, "authorisation");
        assertEquals("12345", form.getReferenceNumber());
        assertEquals("Dispute details", form.getWhyDisputeTheClaim());
        assertEquals("Court Name", form.getHearingCourtLocation());
    }

    @Test
    void testPopulateDetails_NoCourtLocation() {
        CaseLocationCivil caseLocation = new CaseLocationCivil();
        caseLocation.setRegion("2");
        caseLocation.setBaseLocation("000000");
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setResponseCourtCode("121");
        requestedCourt.setReasonForHearingAtSpecificCourt("test");
        requestedCourt.setCaseLocation(caseLocation);
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQRequestedCourt(requestedCourt);
        
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.INDIVIDUAL);
        applicant1.setPartyName("Applicant Name");
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setPartyName("Respondent One");
        
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .respondent1DQ(respondent1DQ)
            .legacyCaseReference("12345")
            .build();
        caseData.setDetailsOfWhyDoesYouDisputeTheClaim("Dispute details");

        given(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).willReturn(Collections.emptyList());

        SealedClaimResponseFormForSpec form = new SealedClaimResponseFormForSpec();
        referenceNumberPopulator.populateReferenceNumberDetails(form, caseData, "authorisation");
        assertNull(form.getHearingCourtLocation());
    }

    @Test
    void testPopulateDetails_NeitherRespondentCourt() {
        CaseLocationCivil caseLocation = new CaseLocationCivil();
        caseLocation.setRegion("2");
        caseLocation.setBaseLocation("000000");
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setResponseCourtCode("121");
        requestedCourt.setReasonForHearingAtSpecificCourt("test");
        requestedCourt.setCaseLocation(caseLocation);
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQRequestedCourt(requestedCourt);
        
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.INDIVIDUAL);
        applicant1.setPartyName("Applicant Name");
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setPartyName("Respondent One");
        
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .respondent1DQ(respondent1DQ)
            .legacyCaseReference("12345")
            .build();
        caseData.setDetailsOfWhyDoesYouDisputeTheClaim("Dispute details");

        SealedClaimResponseFormForSpec form = new SealedClaimResponseFormForSpec();
        referenceNumberPopulator.populateReferenceNumberDetails(form, caseData, "authorisation");
        assertEquals("12345", form.getReferenceNumber());
        assertNull(form.getHearingCourtLocation());
    }
}
