package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.DynamicMultiSelectList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.mediation.MediationCorrespondence;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MEDIATION_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationCorrespondenceType.INBOUND_EMAIL;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationCorrespondenceType.OUTBOUND_TELEPHONE_CALL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.MediationCorrespondenceCallbackHandler.CLAIMANT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.MediationCorrespondenceCallbackHandler.DEFENDANT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.MediationCorrespondenceCallbackHandler.DEFENDANT_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.MediationCorrespondenceCallbackHandler.ERROR_FUTURE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.MediationCorrespondenceCallbackHandler.ERROR_NO_PARTY_SELECTED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.MediationCorrespondenceCallbackHandler.SELECT_ALL;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class MediationCorrespondenceCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String PAGE_ID = "validate-mediation-correspondence";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 28, 10, 30);

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private UserService userService;

    @Mock
    private Time time;

    private MediationCorrespondenceCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MediationCorrespondenceCallbackHandler(objectMapper, userService, time);
    }

    @Test
    void shouldHandleMediationCorrespondenceEvent() {
        assertThat(handler.handledEvents()).containsOnly(MEDIATION_CORRESPONDENCE);
    }

    @Nested
    class AboutToStart {

        @Test
        void shouldPopulateDefendantClaimantAndSelectAll_whenOneVOne() {
            CaseData caseData = oneVOneCase();
            caseData.setMediationCorrespondenceSummary("old summary");

            CaseData updated = getCaseData(handle(caseData, ABOUT_TO_START));

            assertThat(updated.getMediationCorrespondenceParties().getListItems())
                .extracting(DynamicListElement::getLabel)
                .containsExactly("Defendant 1: Mr. Mike Rambo", "Claimant 1: Mr. John Rambo", "Select all");
            assertThat(updated.getMediationCorrespondenceSummary()).isNull();
        }

        @Test
        void shouldPopulateBothDefendants_whenOneVTwo() {
            CaseData caseData = oneVOneCase();
            caseData.setRespondent2(new PartyBuilder().individual("Steve").build());

            CaseData updated = getCaseData(handle(caseData, ABOUT_TO_START));

            assertThat(updated.getMediationCorrespondenceParties().getListItems())
                .extracting(DynamicListElement::getLabel)
                .containsExactly("Defendant 1: Mr. Mike Rambo", "Defendant 2: Mr. Steve Rambo",
                                 "Claimant 1: Mr. John Rambo", "Select all");
        }

        @Test
        void shouldPopulateBothClaimants_whenTwoVOne() {
            CaseData caseData = oneVOneCase();
            caseData.setApplicant2(new PartyBuilder().individual("Jane").build());

            CaseData updated = getCaseData(handle(caseData, ABOUT_TO_START));

            assertThat(updated.getMediationCorrespondenceParties().getListItems())
                .extracting(DynamicListElement::getLabel)
                .containsExactly("Defendant 1: Mr. Mike Rambo", "Claimant 1: Mr. John Rambo",
                                 "Claimant 2: Mr. Jane Rambo", "Select all");
        }
    }

    @Nested
    class MidEvent {

        @BeforeEach
        void setUp() {
            when(time.now()).thenReturn(NOW);
        }

        @Test
        void shouldReturnNoErrors_whenPartySelectedAndDateIsToday() {
            CaseData caseData = completedCase(List.of(CLAIMANT_1));

            AboutToStartOrSubmitCallbackResponse response = handle(caseData, MID);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenDateIsInFuture() {
            CaseData caseData = completedCase(List.of(CLAIMANT_1));
            caseData.setMediationCorrespondenceDate(NOW.toLocalDate().plusDays(1));

            AboutToStartOrSubmitCallbackResponse response = handle(caseData, MID);

            assertThat(response.getErrors()).containsExactly(ERROR_FUTURE_DATE);
        }

        @Test
        void shouldReturnError_whenNoPartySelected() {
            CaseData caseData = completedCase(List.of());

            AboutToStartOrSubmitCallbackResponse response = handle(caseData, MID);

            assertThat(response.getErrors()).containsExactly(ERROR_NO_PARTY_SELECTED);
        }
    }

    @Nested
    class AboutToSubmit {

        @BeforeEach
        void setUp() {
            when(time.now()).thenReturn(NOW);
            when(userService.getUserDetails(anyString()))
                .thenReturn(UserDetails.builder().forename("Case").surname("Worker").build());
        }

        @Test
        void shouldRecordSelectedPartiesAndClearScreenFields() {
            CaseData caseData = completedCase(List.of(CLAIMANT_1, DEFENDANT_1));

            CaseData updated = getCaseData(handle(caseData, ABOUT_TO_SUBMIT));

            assertThat(updated.getMediationCorrespondence()).hasSize(1);
            MediationCorrespondence recorded = updated.getMediationCorrespondence().get(0).getValue();
            assertThat(recorded.getPartiesInvolved()).isEqualTo("Defendant 1: Mr. Mike Rambo, Claimant 1: Mr. John Rambo");
            assertThat(recorded.getCorrespondenceType()).isEqualTo(OUTBOUND_TELEPHONE_CALL);
            assertThat(recorded.getCorrespondenceDate()).isEqualTo(NOW.toLocalDate());
            assertThat(recorded.getSummary()).isEqualTo("Called the claimant");
            assertThat(recorded.getCreatedBy()).isEqualTo("Case Worker");
            assertThat(recorded.getCreatedOn()).isEqualTo(NOW);

            assertThat(updated.getMediationCorrespondenceParties()).isNull();
            assertThat(updated.getMediationCorrespondenceType()).isNull();
            assertThat(updated.getMediationCorrespondenceDate()).isNull();
            assertThat(updated.getMediationCorrespondenceSummary()).isNull();
        }

        @Test
        void shouldRecordAllParties_whenSelectAllChosen() {
            CaseData caseData = completedCase(List.of(SELECT_ALL));
            caseData.setRespondent2(new PartyBuilder().individual("Steve").build());

            CaseData updated = getCaseData(handle(caseData, ABOUT_TO_SUBMIT));

            assertThat(updated.getMediationCorrespondence().get(0).getValue().getPartiesInvolved())
                .isEqualTo("Defendant 1: Mr. Mike Rambo, Defendant 2: Mr. Steve Rambo, Claimant 1: Mr. John Rambo");
        }

        @Test
        void shouldAddNewRecordToStartAndKeepExistingRecords() {
            MediationCorrespondence existing = new MediationCorrespondence()
                .setPartiesInvolved("Defendant 2: Someone")
                .setCorrespondenceType(INBOUND_EMAIL)
                .setCorrespondenceDate(LocalDate.of(2026, 10, 1))
                .setSummary("Earlier email")
                .setCreatedBy("Someone Else")
                .setCreatedOn(LocalDateTime.of(2026, 10, 1, 9, 0));
            CaseData caseData = completedCase(List.of(DEFENDANT_2));
            caseData.setRespondent2(new PartyBuilder().individual("Steve").build());
            caseData.setMediationCorrespondence(wrapElements(existing));

            CaseData updated = getCaseData(handle(caseData, ABOUT_TO_SUBMIT));

            assertThat(updated.getMediationCorrespondence())
                .extracting(Element::getValue)
                .extracting(MediationCorrespondence::getPartiesInvolved)
                .containsExactly("Defendant 2: Mr. Steve Rambo", "Defendant 2: Someone");
            assertThat(updated.getMediationCorrespondence().get(1).getValue()).isEqualTo(existing);
        }
    }

    @Test
    void shouldReturnConfirmation_whenSubmitted() {
        CallbackParams params = callbackParamsOf(oneVOneCase(), SUBMITTED);

        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

        assertThat(response.getConfirmationHeader()).isEqualTo("# Mediation correspondence recorded");
        assertThat(response.getConfirmationBody())
            .contains("What happens next")
            .contains("The details you have entered will be saved on the Summary and Notes tab.");
    }

    private AboutToStartOrSubmitCallbackResponse handle(CaseData caseData, CallbackType type) {
        CallbackParams params = MID.equals(type)
            ? callbackParamsOf(caseData, type, PAGE_ID)
            : callbackParamsOf(caseData, type);
        return (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }

    private CaseData oneVOneCase() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1(new PartyBuilder().individual("John").build());
        caseData.setRespondent1(new PartyBuilder().individual("Mike").build());
        return caseData;
    }

    private CaseData completedCase(List<String> selectedCodes) {
        CaseData caseData = oneVOneCase();
        List<DynamicListElement> selected = selectedCodes.stream()
            .map(code -> DynamicListElement.dynamicElementFromCode(code, code))
            .toList();
        caseData.setMediationCorrespondenceParties(new DynamicMultiSelectList().setValue(selected));
        caseData.setMediationCorrespondenceType(OUTBOUND_TELEPHONE_CALL);
        caseData.setMediationCorrespondenceDate(NOW.toLocalDate());
        caseData.setMediationCorrespondenceSummary("Called the claimant");
        return caseData;
    }
}
