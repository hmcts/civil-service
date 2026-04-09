package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.repositories.CasemanReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class SubmitClaimTaskTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;

    @Mock
    private InterestCalculator interestCalculator;

    @Mock
    private ToggleConfiguration toggleConfiguration;

    @Mock
    private CaseFlagsInitialiser caseFlagInitialiser;

    @Mock
    private FeesService feesService;

    @Mock
    private UserService userService;

    @Mock
    private Time time;

    @Mock
    private CasemanReferenceNumberRepository casemanReferenceNumberRepository;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private AirlineEpimsService airlineEpimsService;

    @Mock
    private LocationReferenceDataService locationRefDataService;

    @InjectMocks
    private SubmitClaimTask submitClaimTask;

    @BeforeEach
    public void setUp() {
        submitClaimTask = new SubmitClaimTask(featureToggleService, new ObjectMapper(), defendantPinToPostLRspecService, interestCalculator,
                                              toggleConfiguration, caseFlagInitialiser, feesService, userService, time, casemanReferenceNumberRepository,
                                              organisationService, airlineEpimsService, locationRefDataService);
    }

    @Test
    void shouldSubmitClaimSuccessfully() {

        Party party = new Party();
        party.setIndividualFirstName("Clay");
        party.setIndividualLastName("Mint");
        party.setPartyName("Clay Mint");
        party.setType(Party.Type.INDIVIDUAL);
        IdamUserDetails idamUserDetails = new IdamUserDetails();
        idamUserDetails.setEmail("test@gmail.com");
        Party partyDetails = new Party();
        partyDetails.setCompanyName("Defendant Inc.");
        partyDetails.setType(Party.Type.COMPANY);
        partyDetails.setPartyName("Defendant Inc.");
        Flags respFlag = new Flags().setPartyName("Defendant").setRoleOnCase("Defendant 1");
        partyDetails.setFlags(respFlag);
        SolicitorReferences solicitorRef = new SolicitorReferences();
        solicitorRef.setRespondentSolicitor1Reference("1234");
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(party)
            .totalClaimAmount(new BigDecimal("1000"))
            .applicantSolicitor1UserDetails(idamUserDetails)
            .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
            .respondent1(partyDetails).build();
        caseData.setSolicitorReferences(solicitorRef);

        when(userService.getUserDetails("authToken")).thenReturn(UserDetails.builder().id("userId").build());
        when(casemanReferenceNumberRepository.next("spec")).thenReturn("12345");

        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setCode("OTHER");
        dynamicListElement.setLabel("OTHER");
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        FlightDelayDetails flightDelayDetails = new FlightDelayDetails();
        flightDelayDetails.setAirlineList(dynamicList);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) submitClaimTask.submitClaim(
            caseData,
            "eventId",
            "authToken",
            YES,
            flightDelayDetails
        );

        assertThat(response.getData()).isNotNull();
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData()).containsEntry("interestClaimUntil", "UNTIL_SETTLED_OR_JUDGEMENT_MADE");
        assertThat(response.getData().get("anyRepresented")).isEqualTo("Yes");
        assertThat(response.getData().get("allPartyNames")).isEqualTo("Clay Mint V Defendant Inc.");
        assertThat(response.getData().get("caseListDisplayDefendantSolicitorReferences"))
            .isEqualTo("1234");
    }

    @Test
    void shouldSubmitClaimSuccessfully1Vs2DS() {
        Party party = new Party();
        party.setIndividualFirstName("Clay");
        party.setIndividualLastName("Mint");
        party.setPartyName("Clay Mint");
        party.setType(Party.Type.INDIVIDUAL);
        IdamUserDetails idamUserDetails = new IdamUserDetails();
        idamUserDetails.setEmail("test@gmail.com");
        Party party1 = new Party();
        party1.setCompanyName("Defendant Inc.");
        party1.setType(Party.Type.COMPANY);
        Party party2 = new Party();
        party2.setIndividualFirstName("Dave");
        party2.setIndividualLastName("Indentoo");
        party2.setType(Party.Type.INDIVIDUAL);
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(party)
            .totalClaimAmount(new BigDecimal("1000"))
            .applicantSolicitor1UserDetails(idamUserDetails)
            .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
            .respondent1(party1)
            .respondent2(party2)
            .respondent2Represented(YES)
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(NO).build();
        SolicitorReferences solicitorRef = new SolicitorReferences();
        solicitorRef.setRespondentSolicitor1Reference("1234");
        solicitorRef.setRespondentSolicitor2Reference("5678");
        caseData.setSolicitorReferences(solicitorRef);

        when(userService.getUserDetails("authToken")).thenReturn(UserDetails.builder().id("userId").build());
        when(casemanReferenceNumberRepository.next("spec")).thenReturn("12345");

        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setCode("OTHER");
        dynamicListElement.setLabel("OTHER");
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        FlightDelayDetails flightDelayDetails = new FlightDelayDetails();
        flightDelayDetails.setAirlineList(dynamicList);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) submitClaimTask.submitClaim(
            caseData,
            "eventId",
            "authToken",
            YES,
            flightDelayDetails
        );

        assertThat(response.getData()).isNotNull();
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData()).containsEntry("interestClaimUntil", "UNTIL_SETTLED_OR_JUDGEMENT_MADE");
        assertThat(response.getData().get("anyRepresented")).isEqualTo("Yes");
        assertThat(response.getData().get("allPartyNames")).isEqualTo("Clay Mint V Defendant Inc., Dave Indentoo");
        assertThat(response.getData().get("caseListDisplayDefendantSolicitorReferences"))
            .isEqualTo("1234, 5678");
    }

    @Test
    void shouldSetTheCourtLocationName() {
        List<LocationRefData> locations = new ArrayList<>();
        locations.add(new LocationRefData().setCourtName("Court Name").setRegionId("2").setEpimmsId("420219")
                          .setSiteName("Civil National Business Centre"));
        when(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).thenReturn(locations);
        Party party = new Party();
        party.setIndividualFirstName("Clay");
        party.setIndividualLastName("Mint");
        party.setPartyName("Clay Mint");
        party.setType(Party.Type.INDIVIDUAL);
        IdamUserDetails idamUserDetails = new IdamUserDetails();
        idamUserDetails.setEmail("test@gmail.com");
        Party party1 = new Party();
        party1.setCompanyName("Defendant Inc.");
        party1.setType(Party.Type.COMPANY);
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(party)
            .totalClaimAmount(new BigDecimal("1000"))
            .applicantSolicitor1UserDetails(idamUserDetails)
            .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
            .respondent1(party1).build();
        SolicitorReferences solicitorRef = new SolicitorReferences();
        solicitorRef.setRespondentSolicitor1Reference("1234");
        caseData.setSolicitorReferences(solicitorRef);

        when(userService.getUserDetails("authToken")).thenReturn(UserDetails.builder().id("userId").build());
        when(casemanReferenceNumberRepository.next("spec")).thenReturn("12345");

        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setCode("OTHER");
        dynamicListElement.setLabel("OTHER");
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        FlightDelayDetails flightDelayDetails = new FlightDelayDetails();
        flightDelayDetails.setAirlineList(dynamicList);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) submitClaimTask.submitClaim(
                caseData,
                "eventId",
                "authToken",
                NO,
                flightDelayDetails
            );
        assertThat(response.getData()).containsEntry("locationName", "Civil National Business Centre");
    }

    @Test
    void shouldCallPinToPostOnlyIfCaseMatched() {
        // Given
        Party party = new Party();
        party.setIndividualFirstName("Clay");
        party.setIndividualLastName("Mint");
        party.setPartyName("Clay Mint");
        party.setType(Party.Type.INDIVIDUAL);
        Party party1 = new Party();
        party1.setIndividualFirstName("John");
        party1.setIndividualLastName("Doe");
        party1.setPartyName("John Doe");
        party1.setType(Party.Type.INDIVIDUAL);
        IdamUserDetails idamUserDetails = new IdamUserDetails();
        idamUserDetails.setEmail("test@gmail.com");
        final CaseData matchedCase = CaseDataBuilder.builder()
            .applicant1(party)
            .respondent1(party1)
            .specRespondent1Represented(NO)
            .addRespondent2(NO)
            .addApplicant2(NO)
            .applicantSolicitor1UserDetails(idamUserDetails)
            .build();

        // When
        when(userService.getUserDetails("authToken")).thenReturn(UserDetails.builder().id("userId").build());
        when(casemanReferenceNumberRepository.next("spec")).thenReturn("12345");
        DefendantPinToPostLRspec defendantPinToPostLRspec = new DefendantPinToPostLRspec();
        defendantPinToPostLRspec.setAccessCode("12345");
        when(defendantPinToPostLRspecService.buildDefendantPinToPost())
            .thenReturn(defendantPinToPostLRspec);

        submitClaimTask.submitClaim(matchedCase, "eventId", "authToken", NO, null);

        // Then
        verify(defendantPinToPostLRspecService).buildDefendantPinToPost();
    }

    @Test
    void shouldNotCallPinToPostIfCaseNotMatched() {
        // Given
        Party party = new Party();
        party.setIndividualFirstName("Clay");
        party.setIndividualLastName("Mint");
        party.setPartyName("Clay Mint");
        party.setType(Party.Type.INDIVIDUAL);
        Party party1 = new Party();
        party1.setCompanyName("Defendant Ltd.");
        party1.setPartyName("Defendant Ltd.");
        party1.setType(Party.Type.COMPANY);
        IdamUserDetails idamUserDetails = new IdamUserDetails();
        idamUserDetails.setEmail("test@gmail.com");
        final CaseData notMatchedCase = CaseDataBuilder.builder()
            .applicant1(party)
            .respondent1(party1)
            .respondent1Represented(YES)
            .addRespondent2(NO)
            .addApplicant2(NO)
            .applicantSolicitor1UserDetails(idamUserDetails)
            .build();

        // When
        when(userService.getUserDetails("authToken")).thenReturn(UserDetails.builder().id("userId").build());
        when(casemanReferenceNumberRepository.next("spec")).thenReturn("12345");

        submitClaimTask.submitClaim(notMatchedCase, "eventId", "authToken", NO, null);

        // Then
        verify(defendantPinToPostLRspecService, never()).buildDefendantPinToPost();
    }
}
