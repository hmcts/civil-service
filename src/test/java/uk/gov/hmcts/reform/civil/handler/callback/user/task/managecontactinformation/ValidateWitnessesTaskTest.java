package uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class ValidateWitnessesTaskTest {

    @InjectMocks
    private ValidateWitnessesTask handler;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private UserService userService;

    private static final UserInfo ADMIN_USER = UserInfo.builder()
        .roles(List.of("caseworker-civil-admin"))
        .uid("admin-uid")
        .build();

    private static final UserInfo LEGAL_REP_USER = UserInfo.builder()
        .roles(List.of("caseworker-civil-solicitor"))
        .uid("solicitor-uid")
        .build();

    private static final String CREATE_ORDER_ERROR_WITNESSES = "Adding a new witness is not permitted in this screen. Please delete any new witnesses.";

    UpdatePartyDetailsForm witness;

    @BeforeEach
    void setup() {
        witness = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name").build();
    }

    @Test
    void shouldNotReturnErrorWhenAdminAddsWitnesses() {
        CaseData caseData = CaseData.builder()
            .updateDetailsForm(UpdateDetailsForm.builder()
                                   .updateWitnessesDetailsForm(wrapElements(UpdatePartyDetailsForm.builder().partyId(null).build()))
                                   .build())
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateWitnesses(caseData, "authToken");

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenNonAdminHasValidWitnesses() {
        CaseData caseData = CaseData.builder()
            .updateDetailsForm(UpdateDetailsForm.builder()
                                   .updateWitnessesDetailsForm(wrapElements(UpdatePartyDetailsForm.builder().partyId("123").build()))
                                   .build())
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateWitnesses(caseData, "authToken");

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNonAdminAddsWitnessesWithoutPartyId() {
        CaseData caseData = CaseData.builder()
            .updateDetailsForm(UpdateDetailsForm.builder()
                                   .updateWitnessesDetailsForm(wrapElements(UpdatePartyDetailsForm.builder().partyId(null).build()))
                                   .build())
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateWitnesses(caseData, "authToken");

        assertThat(response.getErrors()).contains(CREATE_ORDER_ERROR_WITNESSES);
    }

    @Test
    void shouldNotReturnErrorWhenNonAdminHasEmptyUpdateWitnessesDetailsForm() {
        CaseData caseData = CaseData.builder()
            .updateDetailsForm(UpdateDetailsForm.builder()
                                   .updateWitnessesDetailsForm(wrapElements()) // Empty list of witnesses
                                   .build())
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateWitnesses(caseData, "authToken");

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenAdminHasEmptyUpdateWitnessesDetailsForm() {
        CaseData caseData = CaseData.builder()
            .updateDetailsForm(UpdateDetailsForm.builder()
                                   .updateWitnessesDetailsForm(wrapElements()) // Empty list of witnesses
                                   .build())
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.validateWitnesses(caseData, "authToken");

        assertThat(response.getErrors()).isEmpty();
    }

}
