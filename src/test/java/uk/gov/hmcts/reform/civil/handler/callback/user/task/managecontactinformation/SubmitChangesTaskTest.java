package uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.PartyDetailsChangedUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation.SubmitChangesTask.PARTY_NAME_CHANGE_NOT_ALLOWED;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LITIGATION_FRIEND_ID;

@ExtendWith(MockitoExtension.class)
class SubmitChangesTaskTest {

    @InjectMocks
    private SubmitChangesTask handler;
    private ObjectMapper mapper;
    @Mock
    private UserService userService;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseFlagsInitialiser caseFlagsInitialiser;
    @Mock
    private PartyDetailsChangedUtil partyDetailsChangedUtil;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        handler = new SubmitChangesTask(caseDetailsConverter, mapper, userService, caseFlagsInitialiser, partyDetailsChangedUtil);
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void shouldNotReturnErrorForAdminCaseBeforeSubmit() {
        // Admin user with case before Submitted
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setCode(CLAIMANT_ONE_ID);
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setPartyChosen(dynamicList);
        updateDetailsForm.setPartyChosenId("123");

        Flags applicant1Flags = new Flags();
        applicant1Flags.setPartyName("applicant1name");
        applicant1Flags.setRoleOnCase("applicant1");
        Party applicant1 = new PartyBuilder().company().build();
        applicant1.setFlags(applicant1Flags);
        Party respondent1 = new PartyBuilder().company().build();
        Flags respondent1Flags = new Flags();
        respondent1Flags.setPartyName("respondent1name");
        respondent1Flags.setRoleOnCase("Defendant 1");
        respondent1.setFlags(respondent1Flags);

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = CaseDetails.builder().build();
        CaseData oldCaseData = CaseDataBuilder.builder().applicant1(applicant1).respondent1(respondent1).build();
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .updateDetailsForm(updateDetailsForm)
            .ccdCaseReference(123456789L)
            .build();
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(oldCaseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.submitChanges(caseData, caseDetails, "authToken");
        CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(response.getErrors()).isNull();
        assertThat(updatedCaseData.getRespondent1().getFlags()).isEqualTo(respondent1Flags);
        Party respondent1DetailsForClaimDetailsTab = updatedCaseData.getRespondent1DetailsForClaimDetailsTab();
        assertThat(respondent1DetailsForClaimDetailsTab).isNotNull();
        assertThat(respondent1DetailsForClaimDetailsTab.getFlags()).isNull();
    }

    @Test
    void shouldReturnCorrectChangesForAdmin() {
        // Admin user with valid changes
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setCode(CLAIMANT_ONE_ID);
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setPartyChosen(dynamicList);
        updateDetailsForm.setPartyChosenId("123");

        Party applicant1 = new PartyBuilder().company().build();
        Party respondent1 = new PartyBuilder().company().build();
        CaseData caseData = CaseDataBuilder.builder().applicant1(applicant1).respondent1(respondent1).updateDetailsForm(updateDetailsForm).build();
        CaseData oldCaseData = CaseDataBuilder.builder().applicant1(applicant1).respondent1(respondent1).build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = CaseDetails.builder().build();
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(oldCaseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.submitChanges(caseData, caseDetails, "authToken");

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData()).isNotEmpty();
    }

    @Test
    void shouldReturnError_whenLegalRepChangesDefendantName() {
        when(userService.getUserInfo(anyString())).thenReturn(userWithRole("caseworker-civil-solicitor"));

        AboutToStartOrSubmitCallbackResponse response = submitDefendantOneNameChange(DEFENDANT_ONE_ID, "New Company ltd");

        assertThat(response.getErrors()).containsExactly(PARTY_NAME_CHANGE_NOT_ALLOWED);
        assertThat(response.getData()).isNull();
    }

    @Test
    void shouldAllowNameChange_whenSubmittedByCaseworker() {
        when(userService.getUserInfo(anyString())).thenReturn(userWithRole("caseworker-civil-admin"));

        AboutToStartOrSubmitCallbackResponse response = submitDefendantOneNameChange(DEFENDANT_ONE_ID, "New Company ltd");

        CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(response.getErrors()).isNull();
        assertThat(updatedCaseData.getCaseNameHmctsInternal()).isEqualTo("Company ltd v New Company ltd");
    }

    @Test
    void shouldNotReturnError_whenLegalRepUpdatesDefendantWithoutChangingName() {
        AboutToStartOrSubmitCallbackResponse response = submitDefendantOneNameChange(DEFENDANT_ONE_ID, "Company ltd");

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData()).isNotEmpty();
    }

    @Test
    void shouldReturnError_whenLegalRepChangesLitigationFriendName() {
        when(userService.getUserInfo(anyString())).thenReturn(userWithRole("caseworker-civil-solicitor"));
        Party applicant1 = new PartyBuilder().company().build();
        Party respondent1 = new PartyBuilder().individual().build();
        CaseData oldCaseData = CaseDataBuilder.builder().applicant1(applicant1).respondent1(respondent1)
            .addRespondent1LitigationFriend().build();
        CaseData caseData = CaseDataBuilder.builder().applicant1(applicant1).respondent1(respondent1)
            .addRespondent1LitigationFriend()
            .updateDetailsForm(updateDetailsForm(DEFENDANT_ONE_LITIGATION_FRIEND_ID))
            .build();
        caseData.getRespondent1LitigationFriend().setFirstName("Changed");
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(oldCaseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.submitChanges(caseData, caseDetails, "authToken");

        assertThat(response.getErrors()).containsExactly(PARTY_NAME_CHANGE_NOT_ALLOWED);
    }

    private AboutToStartOrSubmitCallbackResponse submitDefendantOneNameChange(String partyChosenId, String newCompanyName) {
        Party applicant1 = new PartyBuilder().company().build();
        Party originalRespondent1 = new PartyBuilder().company().build();
        Party updatedRespondent1 = new PartyBuilder().company().build();
        updatedRespondent1.setCompanyName(newCompanyName);

        CaseData oldCaseData = CaseDataBuilder.builder().applicant1(applicant1).respondent1(originalRespondent1).build();
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(updatedRespondent1)
            .updateDetailsForm(updateDetailsForm(partyChosenId))
            .build();
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(oldCaseData);

        return (AboutToStartOrSubmitCallbackResponse) handler.submitChanges(caseData, caseDetails, "authToken");
    }

    private static UpdateDetailsForm updateDetailsForm(String partyChosenId) {
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setCode(partyChosenId);
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setPartyChosen(dynamicList);
        updateDetailsForm.setPartyChosenId(partyChosenId);
        return updateDetailsForm;
    }

    private static UserInfo userWithRole(String role) {
        return UserInfo.builder().roles(List.of(role)).uid("uid").build();
    }
}
