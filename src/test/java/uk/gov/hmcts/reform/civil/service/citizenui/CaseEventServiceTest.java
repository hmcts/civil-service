package uk.gov.hmcts.reform.civil.service.citizenui;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.HttpStatus.CONFLICT;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter.caseDataContentFromStartEventResponse;

@ExtendWith(SpringExtension.class)
public class CaseEventServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private DraftStoreService draftStoreService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private CaseEventService caseEventService;

    private static final String EVENT_TOKEN = "jM4OWUxMGRkLWEyMzYt";
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
        .id(1L)
        .data(Map.of())
        .build();
    private static final String AUTHORISATION = "authorisation";
    private static final String USER_ID = "123";
    private static final String CASE_ID = "123";
    private static final String DRAFT = "draft";
    private static final String EVENT_ID = "1";
    private static final UUID DRAFT_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final String SUBMISSION_ID = USER_ID + ":" + DRAFT_ID + ":2026-10-12T10:00:00Z";
    private static final StartEventResponse RESPONSE = StartEventResponse
        .builder()
        .eventId(EVENT_ID)
        .token(EVENT_TOKEN)
        .caseDetails(CASE_DETAILS)
        .build();

    @BeforeEach
    void setUp() {
        given(authTokenGenerator.generate()).willReturn(EVENT_TOKEN);
        given(coreCaseDataApi.startEventForCitizen(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(RESPONSE);
        given(coreCaseDataApi.startForCitizen(any(), any(), any(), any(), any(), any()))
            .willReturn(RESPONSE);
        given(coreCaseDataApi.submitEventForCitizen(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willReturn(CASE_DETAILS);
        given(coreCaseDataApi.submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willReturn(CASE_DETAILS);
        ReflectionTestUtils.setField(caseEventService, "caseFlagsLoggingEnabled", false);
    }

    @Test
    void shouldSubmitEventForExistingClaimSuccessfully() {
        InOrder orderVerifier = inOrder(coreCaseDataApi);
        CaseDetails caseDetails = caseEventService.submitEvent(new EventSubmissionParams()
                                                                   .setUpdates(Maps.newHashMap())
                                                                   .setEvent(CaseEvent.DEFENDANT_RESPONSE_SPEC)
                                                                   .setCaseId(CASE_ID)
                                                                   .setUserId(USER_ID)
                                                                   .setAuthorisation(AUTHORISATION));
        assertThat(caseDetails).isEqualTo(CASE_DETAILS);
        orderVerifier.verify(coreCaseDataApi).startEventForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            CaseEvent.DEFENDANT_RESPONSE_SPEC.name()
        );
        orderVerifier.verify(coreCaseDataApi).submitEventForCitizen(AUTHORISATION, EVENT_TOKEN, USER_ID, JURISDICTION,
                                                                    CASE_TYPE, CASE_ID, true,
                                                                    caseDataContentFromStartEventResponse(
                                                                        RESPONSE,
                                                                        Map.of()
                                                                    )
        );
        verifyNoInteractions(draftStoreService, coreCaseDataService);
    }

    @Test
    void shouldSubmitEventForNewClaimSuccessfully() {
        InOrder orderVerifier = inOrder(coreCaseDataApi);
        CaseDetails caseDetails = caseEventService.submitEvent(new EventSubmissionParams()
                                                                   .setUpdates(Maps.newHashMap())
                                                                   .setEvent(CaseEvent.DEFENDANT_RESPONSE_SPEC)
                                                                   .setCaseId(DRAFT)
                                                                   .setUserId(USER_ID)
                                                                   .setAuthorisation(AUTHORISATION));
        assertThat(caseDetails).isEqualTo(CASE_DETAILS);
        orderVerifier.verify(coreCaseDataApi).startForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CaseEvent.DEFENDANT_RESPONSE_SPEC.name()
        );

        orderVerifier.verify(coreCaseDataApi).submitForCitizen(AUTHORISATION, EVENT_TOKEN, USER_ID, JURISDICTION,
                                                               CASE_TYPE, true,
                                                               caseDataContentFromStartEventResponse(
                                                                   RESPONSE,
                                                                   Map.of()
                                                               )
        );
        verifyNoInteractions(draftStoreService, coreCaseDataService);
    }

    @Test
    void shouldClaimThenCreateAndLinkOnFirstSubmitWithSubmissionId() {
        DraftStoreEntity draft = blankDraft();
        given(draftStoreService.getDraftBySubmissionId(SUBMISSION_ID, USER_ID, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.empty());
        given(draftStoreService.getDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_CLAIM)).willReturn(Optional.of(draft));
        given(draftStoreService.claimSubmission(DRAFT_ID, USER_ID, SUBMISSION_ID, DraftType.DRAFT_CLAIM))
            .willReturn(true);
        given(draftStoreService.linkCaseId(DRAFT_ID, USER_ID, "1", DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(draft));

        CaseDetails result = caseEventService.submitEvent(new EventSubmissionParams()
                                                              .setUpdates(Maps.newHashMap())
                                                              .setEvent(CaseEvent.CREATE_LIP_CLAIM)
                                                              .setCaseId(DRAFT)
                                                              .setUserId(USER_ID)
                                                              .setAuthorisation(AUTHORISATION)
                                                              .setSubmissionId(SUBMISSION_ID));

        assertThat(result).isEqualTo(CASE_DETAILS);
        InOrder order = inOrder(draftStoreService, coreCaseDataApi);
        order.verify(draftStoreService).claimSubmission(DRAFT_ID, USER_ID, SUBMISSION_ID, DraftType.DRAFT_CLAIM);
        order.verify(coreCaseDataApi).submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any());
        order.verify(draftStoreService).linkCaseId(DRAFT_ID, USER_ID, "1", DraftType.DRAFT_CLAIM);
    }

    @Test
    void shouldReturnExistingCaseOnSecondSubmitWithSameSubmissionId() {
        DraftStoreEntity linked = blankDraft();
        linked.setCaseId("99");
        linked.setSubmissionId(SUBMISSION_ID);
        CaseDetails existing = CaseDetails.builder().id(99L).build();
        given(draftStoreService.getDraftBySubmissionId(SUBMISSION_ID, USER_ID, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(linked));
        given(coreCaseDataService.getCase(99L, AUTHORISATION)).willReturn(existing);

        CaseDetails result = caseEventService.submitEvent(new EventSubmissionParams()
                                                              .setUpdates(Maps.newHashMap())
                                                              .setEvent(CaseEvent.CREATE_LIP_CLAIM)
                                                              .setCaseId(DRAFT)
                                                              .setUserId(USER_ID)
                                                              .setAuthorisation(AUTHORISATION)
                                                              .setSubmissionId(SUBMISSION_ID));

        assertThat(result).isEqualTo(existing);
        verify(coreCaseDataApi, never()).submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any());
        verify(draftStoreService, never()).claimSubmission(any(), any(), any(), any());
        verify(draftStoreService, never()).linkCaseId(any(), any(), any(), any());
    }

    @Test
    void shouldNotCreateCcdWhenSubmissionAlreadyClaimed() {
        DraftStoreEntity inProgress = blankDraft();
        inProgress.setSubmissionId(SUBMISSION_ID);
        given(draftStoreService.getDraftBySubmissionId(SUBMISSION_ID, USER_ID, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(inProgress));
        given(draftStoreService.getDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(inProgress));
        given(draftStoreService.claimSubmission(DRAFT_ID, USER_ID, SUBMISSION_ID, DraftType.DRAFT_CLAIM))
            .willReturn(false);

        assertThatThrownBy(() -> caseEventService.submitEvent(new EventSubmissionParams()
                                                                  .setUpdates(Maps.newHashMap())
                                                                  .setEvent(CaseEvent.CREATE_LIP_CLAIM)
                                                                  .setCaseId(DRAFT)
                                                                  .setUserId(USER_ID)
                                                                  .setAuthorisation(AUTHORISATION)
                                                                  .setSubmissionId(SUBMISSION_ID)))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(CONFLICT);

        verify(coreCaseDataApi, never()).submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any());
        verify(draftStoreService, never()).linkCaseId(any(), any(), any(), any());
    }

    @Test
    void shouldReturnExistingCaseWhenClaimLostButDraftAlreadyLinked() {
        final DraftStoreEntity blank = blankDraft();
        DraftStoreEntity linked = blankDraft();
        linked.setCaseId("99");
        linked.setSubmissionId(SUBMISSION_ID);
        final CaseDetails existing = CaseDetails.builder().id(99L).build();
        given(draftStoreService.getDraftBySubmissionId(SUBMISSION_ID, USER_ID, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.empty());
        given(draftStoreService.getDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(blank))
            .willReturn(Optional.of(linked));
        given(draftStoreService.claimSubmission(DRAFT_ID, USER_ID, SUBMISSION_ID, DraftType.DRAFT_CLAIM))
            .willReturn(false);
        given(coreCaseDataService.getCase(99L, AUTHORISATION)).willReturn(existing);

        CaseDetails result = caseEventService.submitEvent(new EventSubmissionParams()
                                                              .setUpdates(Maps.newHashMap())
                                                              .setEvent(CaseEvent.CREATE_LIP_CLAIM)
                                                              .setCaseId(DRAFT)
                                                              .setUserId(USER_ID)
                                                              .setAuthorisation(AUTHORISATION)
                                                              .setSubmissionId(SUBMISSION_ID));

        assertThat(result).isEqualTo(existing);
        verify(coreCaseDataApi, never()).submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any());
        verify(draftStoreService, never()).linkCaseId(any(), any(), any(), any());
    }

    @Test
    void shouldCreateNewCaseForDifferentDraftSubmissionId() {
        UUID otherDraftId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        final String otherSubmissionId = USER_ID + ":" + otherDraftId + ":2026-10-12T10:00:00Z";
        DraftStoreEntity otherDraft = blankDraft();
        otherDraft.setId(otherDraftId);
        given(draftStoreService.getDraftBySubmissionId(otherSubmissionId, USER_ID, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.empty());
        given(draftStoreService.getDraft(otherDraftId, USER_ID, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(otherDraft));
        given(draftStoreService.claimSubmission(otherDraftId, USER_ID, otherSubmissionId, DraftType.DRAFT_CLAIM))
            .willReturn(true);
        given(draftStoreService.linkCaseId(eq(otherDraftId), eq(USER_ID), eq("1"), eq(DraftType.DRAFT_CLAIM)))
            .willReturn(Optional.of(otherDraft));

        CaseDetails result = caseEventService.submitEvent(new EventSubmissionParams()
                                                              .setUpdates(Maps.newHashMap())
                                                              .setEvent(CaseEvent.CREATE_LIP_CLAIM)
                                                              .setCaseId(DRAFT)
                                                              .setUserId(USER_ID)
                                                              .setAuthorisation(AUTHORISATION)
                                                              .setSubmissionId(otherSubmissionId));

        assertThat(result).isEqualTo(CASE_DETAILS);
        verify(draftStoreService).claimSubmission(otherDraftId, USER_ID, otherSubmissionId, DraftType.DRAFT_CLAIM);
        verify(draftStoreService).linkCaseId(otherDraftId, USER_ID, "1", DraftType.DRAFT_CLAIM);
    }

    @Test
    void shouldClaimLegacyRedisDraftThenCreateAndLink() {
        final String redisSubmissionId = USER_ID + ":" + USER_ID + ":2026-10-12T10:00:00Z";
        UUID claimRowId = UUID.fromString("99999999-aaaa-bbbb-cccc-dddddddddddd");
        DraftStoreEntity claimRow = blankDraft();
        claimRow.setId(claimRowId);
        claimRow.setSubmissionId(redisSubmissionId);
        given(draftStoreService.getDraftBySubmissionId(redisSubmissionId, USER_ID, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.empty());
        given(draftStoreService.claimLegacySubmission(USER_ID, redisSubmissionId, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(claimRow));
        given(draftStoreService.linkCaseId(claimRowId, USER_ID, "1", DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(claimRow));

        CaseDetails result = caseEventService.submitEvent(new EventSubmissionParams()
                                                              .setUpdates(Maps.newHashMap())
                                                              .setEvent(CaseEvent.CREATE_LIP_CLAIM)
                                                              .setCaseId(DRAFT)
                                                              .setUserId(USER_ID)
                                                              .setAuthorisation(AUTHORISATION)
                                                              .setSubmissionId(redisSubmissionId));

        assertThat(result).isEqualTo(CASE_DETAILS);
        InOrder order = inOrder(draftStoreService, coreCaseDataApi);
        order.verify(draftStoreService).claimLegacySubmission(USER_ID, redisSubmissionId, DraftType.DRAFT_CLAIM);
        order.verify(coreCaseDataApi).submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any());
        order.verify(draftStoreService).linkCaseId(claimRowId, USER_ID, "1", DraftType.DRAFT_CLAIM);
        verify(draftStoreService, never()).claimSubmission(any(), any(), any(), any());
    }

    @Test
    void shouldClaimLegacyRedisDraftWhenUserIdIsUuid() {
        final String uuidUserId = "550e8400-e29b-41d4-a716-446655440000";
        final String redisSubmissionId = uuidUserId + ":" + uuidUserId + ":2026-10-12T10:00:00Z";
        UUID claimRowId = UUID.fromString("99999999-aaaa-bbbb-cccc-dddddddddddd");
        DraftStoreEntity claimRow = blankDraft();
        claimRow.setId(claimRowId);
        claimRow.setUserId(uuidUserId);
        claimRow.setSubmissionId(redisSubmissionId);
        given(draftStoreService.getDraftBySubmissionId(redisSubmissionId, uuidUserId, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.empty());
        given(draftStoreService.claimLegacySubmission(uuidUserId, redisSubmissionId, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(claimRow));
        given(draftStoreService.linkCaseId(claimRowId, uuidUserId, "1", DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(claimRow));

        CaseDetails result = caseEventService.submitEvent(new EventSubmissionParams()
                                                              .setUpdates(Maps.newHashMap())
                                                              .setEvent(CaseEvent.CREATE_LIP_CLAIM)
                                                              .setCaseId(DRAFT)
                                                              .setUserId(uuidUserId)
                                                              .setAuthorisation(AUTHORISATION)
                                                              .setSubmissionId(redisSubmissionId));

        assertThat(result).isEqualTo(CASE_DETAILS);
        verify(draftStoreService).claimLegacySubmission(uuidUserId, redisSubmissionId, DraftType.DRAFT_CLAIM);
        verify(draftStoreService, never()).claimSubmission(any(), any(), any(), any());
        verify(draftStoreService, never()).getDraft(any(), any(), any());
    }

    @Test
    void shouldReturnExistingCaseOnRedisRetryWithSameSubmissionId() {
        final String redisSubmissionId = USER_ID + ":" + USER_ID + ":2026-10-12T10:00:00Z";
        DraftStoreEntity linked = blankDraft();
        linked.setCaseId("99");
        linked.setSubmissionId(redisSubmissionId);
        CaseDetails existing = CaseDetails.builder().id(99L).build();
        given(draftStoreService.getDraftBySubmissionId(redisSubmissionId, USER_ID, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(linked));
        given(coreCaseDataService.getCase(99L, AUTHORISATION)).willReturn(existing);

        CaseDetails result = caseEventService.submitEvent(new EventSubmissionParams()
                                                              .setUpdates(Maps.newHashMap())
                                                              .setEvent(CaseEvent.CREATE_LIP_CLAIM)
                                                              .setCaseId(DRAFT)
                                                              .setUserId(USER_ID)
                                                              .setAuthorisation(AUTHORISATION)
                                                              .setSubmissionId(redisSubmissionId));

        assertThat(result).isEqualTo(existing);
        verify(coreCaseDataApi, never()).submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any());
        verify(draftStoreService, never()).claimLegacySubmission(any(), any(), any());
        verify(draftStoreService, never()).linkCaseId(any(), any(), any(), any());
    }

    @Test
    void shouldNotCreateCcdWhenRedisSubmissionAlreadyClaimed() {
        final String redisSubmissionId = USER_ID + ":" + USER_ID + ":2026-10-12T10:00:00Z";
        DraftStoreEntity inProgress = blankDraft();
        inProgress.setSubmissionId(redisSubmissionId);
        given(draftStoreService.getDraftBySubmissionId(redisSubmissionId, USER_ID, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(inProgress));
        given(draftStoreService.getDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_CLAIM))
            .willReturn(Optional.of(inProgress));

        assertThatThrownBy(() -> caseEventService.submitEvent(new EventSubmissionParams()
                                                                  .setUpdates(Maps.newHashMap())
                                                                  .setEvent(CaseEvent.CREATE_LIP_CLAIM)
                                                                  .setCaseId(DRAFT)
                                                                  .setUserId(USER_ID)
                                                                  .setAuthorisation(AUTHORISATION)
                                                                  .setSubmissionId(redisSubmissionId)))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(CONFLICT);

        verify(coreCaseDataApi, never()).submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any());
        verify(draftStoreService, never()).claimLegacySubmission(any(), any(), any());
        verify(draftStoreService, never()).linkCaseId(any(), any(), any(), any());
    }

    @Test
    void shouldSubmitEventForExistingClaimSuccessfully_whenCaseFlagsLoggingIsEnabled() {
        ReflectionTestUtils.setField(caseEventService, "caseFlagsLoggingEnabled", true);
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .respondent1(new Party()
                             .setFlags(new Flags()
                                        .setPartyName("Mr test")
                                        .setRoleOnCase("Defendant 1")
                                        .setDetails(List.of()))
                             .setType(Party.Type.INDIVIDUAL))
            .build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse eventResponse = StartEventResponse
            .builder()
            .eventId(EVENT_ID)
            .token(EVENT_TOKEN)
            .caseDetails(caseDetails)
            .build();
        given(coreCaseDataApi.startEventForCitizen(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(eventResponse);
        given(caseDetailsConverter.toCaseData(anyMap())).willReturn(caseData);

        CaseDetails response = caseEventService.submitEvent(new EventSubmissionParams()
                                                                   .setUpdates(Maps.newHashMap())
                                                                   .setEvent(CaseEvent.DEFENDANT_RESPONSE_SPEC)
                                                                   .setCaseId(CASE_ID)
                                                                   .setUserId(USER_ID)
                                                                   .setAuthorisation(AUTHORISATION));
        assertThat(response).isEqualTo(CASE_DETAILS);
    }

    private static DraftStoreEntity blankDraft() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return new DraftStoreEntity(
            DRAFT_ID,
            USER_ID,
            null,
            DraftType.DRAFT_CLAIM,
            new HashMap<>(Map.of("step", "ready")),
            now,
            now,
            now.plusDays(30),
            null
        );
    }
}
