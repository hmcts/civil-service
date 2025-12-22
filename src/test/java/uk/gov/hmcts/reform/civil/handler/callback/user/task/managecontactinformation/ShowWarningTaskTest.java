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
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.validation.PartyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;

@ExtendWith(MockitoExtension.class)
class ShowWarningTaskTest {

    @InjectMocks
    private ShowWarningTask handler;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private PartyValidator partyValidator;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private PostcodeValidator postcodeValidator;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        handler = new ShowWarningTask(caseDetailsConverter, mapper, partyValidator, featureToggleService, postcodeValidator);
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void shouldReturnLitigationFriendWarningWhenLitigationFriendExists() {
        // Given a party with a litigation friend
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setCode(CLAIMANT_ONE_ID);
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setPartyChosen(dynamicList);
        Party applicant1 = PartyBuilder.builder().company().build();
        Party respondent1 = PartyBuilder.builder().company().build();
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .updateDetailsForm(updateDetailsForm).build();
        caseData.setApplicant1LitigationFriendRequired(YES);
        CaseData oldCaseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .updateDetailsForm(updateDetailsForm).build();
        oldCaseData.setApplicant1LitigationFriendRequired(YES);
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsBefore = CaseDetails.builder().build();

        when(caseDetailsConverter.toCaseData(caseDetailsBefore)).thenReturn(oldCaseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handler.showWarning(caseData, caseDetailsBefore);

        assertThat(response.getWarnings()).contains("Check the litigation friend's details");
        assertThat(response.getWarnings()).contains("After making these changes, please ensure that the litigation friend's contact information is also up to date.");
    }

    @Test
    void shouldReturnNoWarningForNoLitigationFriend() {
        // Given no litigation friend
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setCode(CLAIMANT_ONE_ID);
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setPartyChosen(dynamicList);

        Party applicant1 = PartyBuilder.builder().company().build();
        Party respondent1 = PartyBuilder.builder().company().build();
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .updateDetailsForm(updateDetailsForm).build();
        caseData.setApplicant1LitigationFriendRequired(NO);
        CaseData oldCaseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .updateDetailsForm(updateDetailsForm).build();
        oldCaseData.setApplicant1LitigationFriendRequired(NO);
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsBefore = CaseDetails.builder().build();

        when(caseDetailsConverter.toCaseData(caseDetailsBefore)).thenReturn(oldCaseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handler.showWarning(caseData, caseDetailsBefore);

        assertThat(response.getWarnings()).isEmpty();
    }

    @Test
    void shouldReturnPostcodeValidationErrorWhenSpecClaim() {
        // Given a SPEC_CLAIM
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setCode(CLAIMANT_ONE_ID);
        dynamicListElement.setLabel("Claimant 1");
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        DynamicListElement dynamicListElement1 = new DynamicListElement();
        dynamicListElement1.setLabel("something");
        dynamicList.setListItems(List.of(dynamicListElement1));
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setPartyChosen(dynamicList);
        UpdatePartyDetailsForm updatePartyDetailsForm = new UpdatePartyDetailsForm();
        updatePartyDetailsForm.setPartyId("123");
        updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(updatePartyDetailsForm));
        Party applicant1 = PartyBuilder.builder().company().build();
        CaseData caseData = CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .updateDetailsForm(updateDetailsForm)
            .applicant1(applicant1)
            .build();
        CaseData oldCaseData = CaseDataBuilder.builder().build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsBefore = CaseDetails.builder().build();

        when(caseDetailsConverter.toCaseData(caseDetailsBefore)).thenReturn(oldCaseData);
        when(postcodeValidator.validate(any())).thenReturn(List.of("Invalid postcode"));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handler.showWarning(caseData, caseDetailsBefore);

        assertThat(response.getErrors()).contains("Invalid postcode");
    }

    @Test
    void shouldValidateNameWhenFeatureToggleEnabled() {
        // Given the feature toggle is enabled and name is too long
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setCode(CLAIMANT_ONE_ID);
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setPartyChosen(dynamicList);
        Party applicant1 = PartyBuilder.builder().individual().build();
        applicant1.setPartyName("seventyoneseventyoneseventyoneseventyoneseventyoneseventyoneseventyoneseventyone");
        Address address = new Address();
        address.setPostCode("1234");
        applicant1.setPrimaryAddress(address);
        CaseData caseData = CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1(applicant1)
            .updateDetailsForm(updateDetailsForm)
            .build();
        CaseData oldCaseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsBefore = CaseDetails.builder().build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(partyValidator.validateName(caseData.getApplicant1().getPartyName(), List.of())).thenReturn(List.of("Name exceeds maximum length 70"));
        when(caseDetailsConverter.toCaseData(caseDetailsBefore)).thenReturn(oldCaseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handler.showWarning(caseData, caseDetailsBefore);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).contains("Name exceeds maximum length 70");
    }

    @Test
    void shouldValidateAddressWhenFeatureToggleEnabled() {
        // Given the feature toggle is enabled and address is too long
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setCode(CLAIMANT_ONE_ID);
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setPartyChosen(dynamicList);

        Party applicant1 = PartyBuilder.builder().individual().build();
        applicant1.setBulkClaimPartyName("bulk claim party name");
        Address address = new Address();
        address.setAddressLine1("Line 1 test again for more than 35 characters test test test test test");
        address.setAddressLine2("Line 1 test again for more than 35 characters test test");
        address.setAddressLine3("Line 1 test again for more than 35 characters");
        address.setCounty("Line 1 test again for more than 35 characters");
        address.setPostCode("Line 1 test again for more than 35 characters");
        address.setPostTown("Line 1 test again for more than 35 characters");
        applicant1.setPrimaryAddress(address);
        CaseData caseData = CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1(applicant1)
            .updateDetailsForm(updateDetailsForm)
            .build();
        CaseData oldCaseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsBefore = CaseDetails.builder().build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        List<String> errorList = List.of("exceeds maximum length 35");
        when(partyValidator.validateAddress(any(Address.class), anyList())).thenReturn(errorList);
        when(partyValidator.validateName(applicant1.getPartyName(), errorList)).thenReturn(errorList);

        when(caseDetailsConverter.toCaseData(caseDetailsBefore)).thenReturn(oldCaseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handler.showWarning(caseData, caseDetailsBefore);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).contains("exceeds maximum length 35");
    }

    @Test
    void shouldReturnLitigationFriendUpdateWarning() {
        // Given both claimants have litigation friends
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setCode(CLAIMANT_ONE_ID);
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setPartyChosen(dynamicList);
        Party applicant1 = PartyBuilder.builder().company().build();
        Party applicant2 = PartyBuilder.builder().company().build();
        LitigationFriend litigationFriend = new LitigationFriend();
        CaseData oldCaseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .applicant2(applicant2)
            .updateDetailsForm(updateDetailsForm).build();
        oldCaseData.setApplicant1LitigationFriend(litigationFriend);
        oldCaseData.setApplicant1LitigationFriendRequired(YES);
        oldCaseData.setApplicant2LitigationFriend(litigationFriend);
        oldCaseData.setApplicant2LitigationFriendRequired(YES);
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .applicant2(applicant2)
            .updateDetailsForm(updateDetailsForm).build();
        caseData.setApplicant1LitigationFriend(litigationFriend);
        caseData.setApplicant1LitigationFriendRequired(YES);
        caseData.setApplicant2LitigationFriend(litigationFriend);
        caseData.setApplicant2LitigationFriendRequired(YES);
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsBefore = CaseDetails.builder().build();
        when(caseDetailsConverter.toCaseData(caseDetailsBefore)).thenReturn(oldCaseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handler.showWarning(caseData, caseDetailsBefore);

        assertThat(response.getWarnings()).contains("Check the litigation friend's details",
                                                    "After making these changes, please ensure that the litigation friend's contact information is also up to date.");
    }
}
