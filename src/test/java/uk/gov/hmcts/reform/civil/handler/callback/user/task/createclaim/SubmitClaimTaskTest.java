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
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.repositories.SpecReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
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
    private SpecReferenceNumberRepository specReferenceNumberRepository;

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
                                              toggleConfiguration, caseFlagInitialiser, feesService, userService, time, specReferenceNumberRepository,
                                              organisationService, airlineEpimsService, locationRefDataService);
    }

    @Test
    void shouldSubmitClaimSuccessfully() {

        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .individualFirstName("Clay")
                            .individualLastName("Mint")
                            .partyName("Clay Mint")
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .totalClaimAmount(new BigDecimal("1000"))
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("test@gmail.com").build())
            .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
            .respondent1(Party.builder().companyName("Defendant Inc.").type(Party.Type.COMPANY).build())
            .solicitorReferences(SolicitorReferences.builder().respondentSolicitor1Reference("1234").build())
            .build();

        when(userService.getUserDetails("authToken")).thenReturn(UserDetails.builder().id("userId").build());
        when(specReferenceNumberRepository.getSpecReferenceNumber()).thenReturn("12345");

        FlightDelayDetails flightDelayDetails = FlightDelayDetails.builder()
            .airlineList(
                DynamicList.builder()
                    .value(DynamicListElement.builder().code("OTHER").label("OTHER")
                               .build()).build()).build();

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

        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .individualFirstName("Clay")
                            .individualLastName("Mint")
                            .partyName("Clay Mint")
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .totalClaimAmount(new BigDecimal("1000"))
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("test@gmail.com").build())
            .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
            .respondent1(Party.builder().companyName("Defendant Inc.").type(Party.Type.COMPANY).build())
            .respondent2(Party.builder().individualFirstName("Dave").individualLastName("Indentoo").type(Party.Type.INDIVIDUAL).build())
            .respondent2Represented(YES)
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(NO)
            .solicitorReferences(SolicitorReferences.builder()
                                     .respondentSolicitor1Reference("1234")
                                     .respondentSolicitor2Reference("5678")
                                     .build())
            .build();

        when(userService.getUserDetails("authToken")).thenReturn(UserDetails.builder().id("userId").build());
        when(specReferenceNumberRepository.getSpecReferenceNumber()).thenReturn("12345");

        FlightDelayDetails flightDelayDetails = FlightDelayDetails.builder()
            .airlineList(
                DynamicList.builder()
                    .value(DynamicListElement.builder().code("OTHER").label("OTHER")
                               .build()).build()).build();

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
}

