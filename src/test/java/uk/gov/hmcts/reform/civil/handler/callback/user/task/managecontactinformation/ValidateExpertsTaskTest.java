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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
        party = new UpdatePartyDetailsForm();
        party.setFirstName("First");
        party.setLastName("Name");
        dqExpert = new Expert();
        dqExpert.setPartyID("id");
        dqExpert.setFirstName("dq");
        dqExpert.setLastName("dq");
        expectedExpert1 = new Expert();
        expectedExpert1.setFirstName("First");
        expectedExpert1.setLastName("Name");
        expectedExpert1.setEventAdded("Manage Contact Information Event");
        expectedExpert1.setDateAdded(LocalDate.now());
        expectedExpert1.setPartyID(PARTY_ID);
        expectedExpertFlags = new PartyFlagStructure();
        expectedExpertFlags.setPartyID(PARTY_ID);
        expectedExpertFlags.setFirstName("First");
        expectedExpertFlags.setLastName("Name");
        dqWitness = new Witness();
        dqWitness.setFirstName("dq");
        dqWitness.setLastName("dq");
        dqWitness.setPartyID("id");
        expectedWitness1 = new Witness();
        expectedWitness1.setFirstName("First");
        expectedWitness1.setLastName("Name");
        expectedWitness1.setEventAdded("Manage Contact Information Event");
        expectedWitness1.setDateAdded(LocalDate.now());
        expectedWitness1.setPartyID(PARTY_ID);
        expectedWitnessFlags = new PartyFlagStructure();
        expectedWitnessFlags.setPartyID(PARTY_ID);
        expectedWitnessFlags.setFirstName("First");
        expectedWitnessFlags.setLastName("Name");
    }

    @Test
    void shouldNotReturnErrorWhenAdminAddsExperts() {
        UpdatePartyDetailsForm updatePartyDetailsForm = new UpdatePartyDetailsForm();
        updatePartyDetailsForm.setPartyId(null);
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(updatePartyDetailsForm));
        CaseData caseData = CaseDataBuilder.builder()
            .updateDetailsForm(updateDetailsForm)
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateExperts(caseData, "authToken");

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenNonAdminHasValidExperts() {
        UpdatePartyDetailsForm updatePartyDetailsForm = new UpdatePartyDetailsForm();
        updatePartyDetailsForm.setPartyId("123");
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(updatePartyDetailsForm));
        CaseData caseData = CaseDataBuilder.builder()
            .updateDetailsForm(updateDetailsForm)
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateExperts(caseData, "authToken");

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNonAdminAddsExpertsWithoutPartyId() {
        UpdatePartyDetailsForm updatePartyDetailsForm = new UpdatePartyDetailsForm();
        updatePartyDetailsForm.setPartyId(null);
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(updatePartyDetailsForm));
        CaseData caseData = CaseDataBuilder.builder()
            .updateDetailsForm(updateDetailsForm)
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateExperts(caseData, "authToken");

        assertThat(response.getErrors()).contains(CREATE_ORDER_ERROR_EXPERTS);
    }

    @Test
    void shouldNotReturnErrorWhenNoExpertsInUpdateDetailsForm() {
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setUpdateExpertsDetailsForm(null); // No experts form
        CaseData caseData = CaseDataBuilder.builder()
            .updateDetailsForm(updateDetailsForm)
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateExperts(caseData, "authToken");

        assertThat(response.getErrors()).isEmpty();
    }
}
