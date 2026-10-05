package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.helpers;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class ReferenceNumberAndCourtDetailsPopulatorTest {

    @InjectMocks
    private ReferenceNumberAndCourtDetailsPopulator referenceNumberPopulator;

    @Mock
    private LocationReferenceDataService locationRefDataService;

    private static final List<LocationRefData> LOCATIONS = List.of(new LocationRefData().setSiteName("SiteName").setCourtAddress("1").setPostcode("1")
                                                                       .setCourtName("Court Name").setRegion("Region").setRegionId("4").setCourtVenueId("000")
                                                                       .setCourtTypeId("10").setCourtLocationCode("121")
                                                                       .setEpimmsId("000000"));

    @Test
    void testPopulateDetails_Respondent1() {
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.INDIVIDUAL);
        applicant1.setPartyName("Applicant Name");
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setPartyName("Respondent Name");

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .respondent1DQ(getRespondent1DQ())
            .legacyCaseReference("12345")
            .ccdCaseReference(1234567890123456L)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();
        caseData.setDetailsOfWhyDoesYouDisputeTheClaim("Dispute details");

        given(locationRefDataService.getCourtLocationsByEpimmsId(any(), any(), any())).willReturn(LOCATIONS);

        SealedClaimResponseFormForSpec form = new SealedClaimResponseFormForSpec();
        referenceNumberPopulator.populateReferenceNumberDetails(form, caseData, "authorisation");
        assertEquals("12345", form.getReferenceNumber());
        assertEquals("1234567890123456", form.getCcdCaseReference());
        assertEquals("Dispute details", form.getWhyDisputeTheClaim());
        assertEquals("Court Name", form.getHearingCourtLocation());
    }

    private static @NonNull Respondent1DQ getRespondent1DQ() {
        CaseLocationCivil caseLocation = new CaseLocationCivil();
        caseLocation.setRegion("2");
        caseLocation.setBaseLocation("000000");
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setResponseCourtCode("121");
        requestedCourt.setReasonForHearingAtSpecificCourt("test");
        requestedCourt.setCaseLocation(caseLocation);
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQRequestedCourt(requestedCourt);
        return respondent1DQ;
    }

    @Test
    void testPopulateDetails_Respondent2() {
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
            .respondent1DQ(getRespondent1DQ())
            .respondent2DQ(getRespondent2DQ())
            .legacyCaseReference("12345")
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent2ResponseDate(LocalDateTime.now().plusDays(1))
            .build();
        caseData.setDetailsOfWhyDoesYouDisputeTheClaim2("Dispute details");

        given(locationRefDataService.getCourtLocationsByEpimmsId(any(), any(), any())).willReturn(LOCATIONS);

        SealedClaimResponseFormForSpec form = new SealedClaimResponseFormForSpec();
        referenceNumberPopulator.populateReferenceNumberDetails(form, caseData, "authorisation");
        assertEquals("12345", form.getReferenceNumber());
        assertEquals("Dispute details", form.getWhyDisputeTheClaim());
        assertEquals("Court Name", form.getHearingCourtLocation());
    }

    private static @NonNull Respondent2DQ getRespondent2DQ() {
        CaseLocationCivil caseLocation2 = new CaseLocationCivil();
        caseLocation2.setRegion("2");
        caseLocation2.setBaseLocation("000000");
        RequestedCourt requestedCourt2 = new RequestedCourt();
        requestedCourt2.setResponseCourtCode("121");
        requestedCourt2.setReasonForHearingAtSpecificCourt("test");
        requestedCourt2.setCaseLocation(caseLocation2);
        Respondent2DQ respondent2DQ = new Respondent2DQ();
        respondent2DQ.setRespondent2DQRequestedCourt(requestedCourt2);
        return respondent2DQ;
    }

    @Test
    void testPopulateDetails_NoCourtLocation() {
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.INDIVIDUAL);
        applicant1.setPartyName("Applicant Name");
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setPartyName("Respondent One");

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .respondent1DQ(getRespondent1DQ())
            .legacyCaseReference("12345")
            .build();
        caseData.setDetailsOfWhyDoesYouDisputeTheClaim("Dispute details");

        given(locationRefDataService.getCourtLocationsByEpimmsId(any(), any(), any())).willReturn(Collections.emptyList());

        SealedClaimResponseFormForSpec form = new SealedClaimResponseFormForSpec();
        referenceNumberPopulator.populateReferenceNumberDetails(form, caseData, "authorisation");
        assertNull(form.getHearingCourtLocation());
    }

    @Test
    void testPopulateDetails_NeitherRespondentCourt() {
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.INDIVIDUAL);
        applicant1.setPartyName("Applicant Name");
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setPartyName("Respondent One");

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .respondent1DQ(getRespondent1DQ())
            .legacyCaseReference("12345")
            .build();
        caseData.setDetailsOfWhyDoesYouDisputeTheClaim("Dispute details");

        SealedClaimResponseFormForSpec form = new SealedClaimResponseFormForSpec();
        referenceNumberPopulator.populateReferenceNumberDetails(form, caseData, "authorisation");
        assertEquals("12345", form.getReferenceNumber());
        assertNull(form.getHearingCourtLocation());
    }

    @Test
    void testPopulateDetails_DoesNotLookupCourtWhenRequestedCourtMissing() {
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.INDIVIDUAL);
        applicant1.setPartyName("Applicant Name");
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setPartyName("Respondent One");
        Respondent1DQ respondent1DQ = new Respondent1DQ();

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
        verify(locationRefDataService, never()).getCourtLocationsByEpimmsId(any(), any(), any());
    }

    @Test
    void testPopulateDetails_DoesNotLookupCourtWhenRequestedCourtCaseLocationMissing() {
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.INDIVIDUAL);
        applicant1.setPartyName("Applicant Name");
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setPartyName("Respondent One");
        RequestedCourt requestedCourt = new RequestedCourt();
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQRequestedCourt(requestedCourt);

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
        verify(locationRefDataService, never()).getCourtLocationsByEpimmsId(any(), any(), any());
    }
}
