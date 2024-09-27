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
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.PartyDetailsChangedUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;

@ExtendWith(MockitoExtension.class)
public class SubmitChangesTaskTest {

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
    @Mock
    private CoreCaseDataService coreCaseDataService;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        handler = new SubmitChangesTask(caseDetailsConverter, mapper, userService, caseFlagsInitialiser, partyDetailsChangedUtil,
                                        coreCaseDataService
        );
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void shouldNotReturnErrorForAdminCaseBeforeSubmit() {
        // Admin user with case before Submitted
        UpdateDetailsForm updateDetailsForm = UpdateDetailsForm.builder()
            .partyChosen(DynamicList.builder()
                             .value(DynamicListElement.builder()
                                        .code(CLAIMANT_ONE_ID)
                                        .build())
                             .build())
            .partyChosenId("123")
            .build();
        Flags applicant1Flags = Flags.builder().partyName("applicant1name").roleOnCase("applicant1").build();
        Party applicant1 = PartyBuilder.builder().company().build();
        applicant1.setFlags(applicant1Flags);
        Party respondent1 = PartyBuilder.builder().company().build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = CaseDetails.builder().build();
        CaseData oldCaseData = CaseData.builder().applicant1(applicant1).respondent1(respondent1).build();
        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .updateDetailsForm(updateDetailsForm)
            .ccdCaseReference(123456789L)
            .build();
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(oldCaseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.submitChanges(caseData, caseDetails, "authToken");

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnCorrectChangesForAdmin() {
        // Admin user with valid changes
        UpdateDetailsForm updateDetailsForm = UpdateDetailsForm.builder()
            .partyChosen(DynamicList.builder()
                             .value(DynamicListElement.builder()
                                        .code(CLAIMANT_ONE_ID)
                                        .build())
                             .build())
            .partyChosenId("123")
            .build();
        Party applicant1 = PartyBuilder.builder().company().build();
        Party respondent1 = PartyBuilder.builder().company().build();
        CaseData caseData = CaseData.builder().applicant1(applicant1).respondent1(respondent1).updateDetailsForm(updateDetailsForm).build();
        CaseData oldCaseData = CaseData.builder().applicant1(applicant1).respondent1(respondent1).build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = CaseDetails.builder().build();
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(oldCaseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.submitChanges(caseData, caseDetails, "authToken");

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData()).isNotEmpty();
    }

}
