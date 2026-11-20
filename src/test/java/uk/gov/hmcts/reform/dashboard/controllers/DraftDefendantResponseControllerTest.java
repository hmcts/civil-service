package uk.gov.hmcts.reform.dashboard.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.dashboard.data.DraftDefendantResponseRequest;
import uk.gov.hmcts.reform.dashboard.data.DraftDefendantResponseResponse;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftDefendantResponseAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftDefendantResponseNotFoundException;
import uk.gov.hmcts.reform.dashboard.services.DraftDefendantResponseService;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftDefendantResponseControllerTest {

    private static final String USER_ID = "USER1";
    private static final String CASE_ID = "12345";
    private static final UUID DRAFT_ID = UUID.randomUUID();
    private static final String AUTH = "Token";

    @Mock
    private DraftDefendantResponseService draftResponseService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DraftDefendantResponseController controller;

    private DraftStoreEntity draftStoreEntity;
    private Map<String, Object> payload;

    @BeforeEach
    void init() {
        lenient().when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid(USER_ID).build());
        payload = new HashMap<>();
        payload.put("step", "response-details");
        draftStoreEntity = new DraftStoreEntity();
        draftStoreEntity.setId(DRAFT_ID);
        draftStoreEntity.setCaseId(CASE_ID);
        draftStoreEntity.setUserId(USER_ID);
        draftStoreEntity.setPayload(new HashMap<>(payload));
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-11-06T10:00:00Z");
        draftStoreEntity.setCreatedAt(createdAt);
        draftStoreEntity.setUpdatedAt(createdAt);
        draftStoreEntity.setExpiresAt(createdAt.plusDays(180));
    }

    @Test
    void shouldReturnCreatedWhenNewDraftIsCreated() {
        when(draftResponseService.createDraftResponse(USER_ID, CASE_ID, payload)).thenReturn(draftStoreEntity);

        ResponseEntity<DraftDefendantResponseResponse> response =
            controller.createDraftResponse(AUTH, new DraftDefendantResponseRequest(CASE_ID, payload));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDraftId()).isEqualTo(DRAFT_ID);
        assertThat(response.getBody().getCaseId()).isEqualTo(CASE_ID);
        assertThat(response.getBody().getPayload()).containsEntry("step", "response-details");
        verify(draftResponseService).createDraftResponse(USER_ID, CASE_ID, payload);
    }

    @Test
    void shouldReturnConflictWhenDraftAlreadyExists() {
        ResponseEntity<String> response = controller.draftDefendantResponseAlreadyExists(
            new DraftDefendantResponseAlreadyExistsException(new IllegalStateException("duplicate"))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("An active draft response already exists");
    }

    @Test
    void shouldReturnDraftForCaseWhenItExists() {
        when(draftResponseService.getDraftResponseForCase(USER_ID, CASE_ID)).thenReturn(Optional.of(draftStoreEntity));

        ResponseEntity<DraftDefendantResponseResponse> response = controller.getDraftResponseForCase(CASE_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCaseId()).isEqualTo(CASE_ID);
    }

    @Test
    void shouldThrowNotFoundWhenNoDraftExistsForCase() {
        when(draftResponseService.getDraftResponseForCase(USER_ID, CASE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getDraftResponseForCase(CASE_ID, AUTH))
            .isInstanceOf(DraftDefendantResponseNotFoundException.class)
            .hasMessage("No active draft defendant response found for caseId " + CASE_ID);
    }

    @Test
    void shouldReturnDraftWhenItExists() {
        when(draftResponseService.getDraftResponse(DRAFT_ID, USER_ID)).thenReturn(Optional.of(draftStoreEntity));

        ResponseEntity<DraftDefendantResponseResponse> response = controller.getDraftResponse(DRAFT_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDraftId()).isEqualTo(DRAFT_ID);
        verify(draftResponseService).getDraftResponse(DRAFT_ID, USER_ID);
    }

    @Test
    void shouldThrowNotFoundWhenDraftDoesNotExist() {
        when(draftResponseService.getDraftResponse(DRAFT_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getDraftResponse(DRAFT_ID, AUTH))
            .isInstanceOf(DraftDefendantResponseNotFoundException.class)
            .hasMessage("No active draft defendant response found for draftId " + DRAFT_ID);
    }

    @Test
    void shouldReturnUpdatedDraftWhenDraftExists() {
        DraftDefendantResponseRequest request = new DraftDefendantResponseRequest(CASE_ID, payload);
        when(draftResponseService.updateResponseDraft(DRAFT_ID, USER_ID, CASE_ID, payload)).thenReturn(draftStoreEntity);

        ResponseEntity<DraftDefendantResponseResponse> response =
            controller.updateDraftResponse(DRAFT_ID, AUTH, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(draftResponseService).updateResponseDraft(DRAFT_ID, USER_ID, CASE_ID, payload);
    }

    @Test
    void shouldReturnNoContentWhenDraftIsDeleted() {
        ResponseEntity<Void> response = controller.deleteDraftResponse(DRAFT_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(draftResponseService).deleteDraftResponse(DRAFT_ID, USER_ID);
    }
}
