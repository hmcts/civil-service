package uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class ValidateExpertsTaskTest {

    @InjectMocks
    private ValidateExpertsTask handler;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    private ObjectMapper mapper;
    @Mock
    private UserService userService;
    private static final UserInfo ADMIN_USER = UserInfo.builder()
        .roles(List.of("caseworker-civil-admin"))
        .uid("uid")
        .build();

    private static final UserInfo LEGAL_REP_USER = UserInfo.builder()
        .roles(List.of("caseworker-civil-solicitor"))
        .uid("uid")
        .build();
    private static final String CREATE_ORDER_ERROR_EXPERTS = "Adding a new expert is not permitted in this screen. Please delete any new experts.";

    UpdatePartyDetailsForm party;
    Expert dqExpert;
    Expert expectedExpert1;
    Witness dqWitness;
    Witness expectedWitness1;
    PartyFlagStructure expectedExpertFlags;
    PartyFlagStructure expectedWitnessFlags;
    private static final String PARTY_ID = "party-id";

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        handler = new ValidateExpertsTask(mapper, userService);
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name").build();
        dqExpert = Expert.builder().partyID("id").firstName("dq").lastName("dq").build();
        expectedExpert1 = dqExpert.builder().firstName("First").lastName("Name")
            .eventAdded("Manage Contact Information Event").dateAdded(LocalDate.now())
            .partyID(PARTY_ID)
            .build();
        expectedExpertFlags = PartyFlagStructure.builder()
            .partyID(PARTY_ID)
            .firstName("First")
            .lastName("Name")
            .build();
        dqWitness = Witness.builder().firstName("dq").lastName("dq").partyID("id").build();
        expectedWitness1 = Witness.builder().firstName("First").lastName("Name")
            .eventAdded("Manage Contact Information Event").dateAdded(LocalDate.now())
            .partyID(PARTY_ID).build();
        expectedWitnessFlags = PartyFlagStructure.builder()
            .partyID(PARTY_ID)
            .firstName("First")
            .lastName("Name")
            .build();
    }

    @Test
    void shouldNotReturnErrorWhenAdminAddsExperts() {
        CaseData caseData = CaseData.builder()
            .updateDetailsForm(UpdateDetailsForm.builder()
                                   .updateExpertsDetailsForm(wrapElements(UpdatePartyDetailsForm.builder().partyId(null).build()))
                                   .build())
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateExperts(caseData, "authToken");

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenNonAdminHasValidExperts() {
        CaseData caseData = CaseData.builder()
            .updateDetailsForm(UpdateDetailsForm.builder()
                                   .updateExpertsDetailsForm(wrapElements(UpdatePartyDetailsForm.builder().partyId("123").build()))
                                   .build())
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateExperts(caseData, "authToken");

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNonAdminAddsExpertsWithoutPartyId() {
        CaseData caseData = CaseData.builder()
            .updateDetailsForm(UpdateDetailsForm.builder()
                                   .updateExpertsDetailsForm(wrapElements(UpdatePartyDetailsForm.builder().partyId(null).build()))
                                   .build())
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateExperts(caseData, "authToken");

        assertThat(response.getErrors()).contains(CREATE_ORDER_ERROR_EXPERTS);
    }

    @Test
    void shouldNotReturnErrorWhenNoExpertsInUpdateDetailsForm() {
        CaseData caseData = CaseData.builder()
            .updateDetailsForm(UpdateDetailsForm.builder()
                                   .updateExpertsDetailsForm(null) // No experts form
                                   .build())
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateExperts(caseData, "authToken");

        assertThat(response.getErrors()).isEmpty();
    }
}
