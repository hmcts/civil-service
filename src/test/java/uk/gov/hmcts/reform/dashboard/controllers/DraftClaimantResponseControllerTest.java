package uk.gov.hmcts.reform.dashboard.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.dashboard.data.DraftClaimantResponseRequest;
import uk.gov.hmcts.reform.dashboard.data.DraftClaimantResponseResponse;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimantResponseAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimantResponseNotFoundException;
import uk.gov.hmcts.reform.dashboard.services.DraftClaimantResponseService;
import uk.gov.hmcts.reform.draftstore.DraftType;
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
class DraftClaimantResponseControllerTest {

    private static final String USER_ID = "USER1";
    private static final String CASE_ID = "1234567890123456";
    private static final UUID DRAFT_ID = UUID.randomUUID();
    private static final String AUTH = "Token";

    @Mock
    private DraftClaimantResponseService draftResponseService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DraftClaimantResponseController controller;

    private DraftStoreEntity draftStoreEntity;
    private Map<String, Object> payload;

    @BeforeEach
    void init() {
        lenient().when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid(USER_ID).build());
        payload = new HashMap<>();
        payload.put("draftClaimCacheTtlDays", 30);
        OffsetDateTime now = OffsetDateTime.now();
        draftStoreEntity = new DraftStoreEntity(
            DRAFT_ID,
            USER_ID,
            CASE_ID,
            DraftType.DRAFT_CLAIMANT_RESPONSE,
            payload,
            now,
            now,
            now.plusDays(30),
            null
        );
    }

    @Test
    void shouldReturnCreatedWhenNewDraftIsCreated() {
        when(draftResponseService.createDraftResponse(USER_ID, CASE_ID, payload)).thenReturn(draftStoreEntity);

        ResponseEntity<DraftClaimantResponseResponse> response =
            controller.createDraftResponse(AUTH, new DraftClaimantResponseRequest(CASE_ID, payload));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDraftId()).isEqualTo(DRAFT_ID);
        assertThat(response.getBody().getCaseId()).isEqualTo(CASE_ID);
        verify(draftResponseService).createDraftResponse(USER_ID, CASE_ID, payload);
    }

    @Test
    void shouldReturnConflictWhenDraftAlreadyExists() {
        ResponseEntity<String> response = controller.draftClaimantResponseAlreadyExists(
            new DraftClaimantResponseAlreadyExistsException(new DataIntegrityViolationException("duplicate"))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("An active draft claimant response already exists");
    }

    @Test
    void shouldReturnDraftForCaseWhenItExists() {
        when(draftResponseService.getDraftResponseForCase(USER_ID, CASE_ID)).thenReturn(Optional.of(draftStoreEntity));

        ResponseEntity<DraftClaimantResponseResponse> response = controller.getDraftResponseForCase(CASE_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCaseId()).isEqualTo(CASE_ID);
    }

    @Test
    void shouldThrowNotFoundWhenNoDraftExistsForCase() {
        when(draftResponseService.getDraftResponseForCase(USER_ID, CASE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getDraftResponseForCase(CASE_ID, AUTH))
            .isInstanceOf(DraftClaimantResponseNotFoundException.class);
    }

    @Test
    void shouldReturnDraftWhenItExists() {
        when(draftResponseService.getDraftResponse(DRAFT_ID, USER_ID)).thenReturn(Optional.of(draftStoreEntity));

        ResponseEntity<DraftClaimantResponseResponse> response = controller.getDraftResponse(DRAFT_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(draftResponseService).getDraftResponse(DRAFT_ID, USER_ID);
    }

    @Test
    void shouldThrowNotFoundWhenDraftIsMissing() {
        when(draftResponseService.getDraftResponse(DRAFT_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getDraftResponse(DRAFT_ID, AUTH))
            .isInstanceOf(DraftClaimantResponseNotFoundException.class);
    }

    @Test
    void shouldReturnUpdatedDraftWhenDraftExists() {
        DraftClaimantResponseRequest request = new DraftClaimantResponseRequest(CASE_ID, payload);
        when(draftResponseService.updateDraftResponse(DRAFT_ID, USER_ID, CASE_ID, payload))
            .thenReturn(draftStoreEntity);

        ResponseEntity<DraftClaimantResponseResponse> response =
            controller.updateDraftResponse(DRAFT_ID, AUTH, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(draftResponseService).updateDraftResponse(DRAFT_ID, USER_ID, CASE_ID, payload);
    }

    @Test
    void shouldReturnNoContentWhenDraftIsDeleted() {
        ResponseEntity<Void> response = controller.deleteDraftResponse(DRAFT_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(draftResponseService).deleteDraftResponse(DRAFT_ID, USER_ID);
    }
}
