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
import uk.gov.hmcts.reform.dashboard.data.DraftClaimRequest;
import uk.gov.hmcts.reform.dashboard.data.DraftClaimResponse;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimNotFoundException;
import uk.gov.hmcts.reform.dashboard.services.DraftClaimService;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftClaimControllerTest {

    private static final String USER_ID = "USER1";
    private static final String CASE_ID = "CCD123";
    private static final UUID DRAFT_ID = UUID.randomUUID();
    private static final String AUTH = "Token";
    @Mock
    private DraftClaimService draftClaimService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DraftClaimController controller;

    private DraftStoreEntity draftStoreEntity;
    private Map<String, Object> payload;

    @BeforeEach
    void init() {
        // Lenient: the bad-request and conflict tests never resolve the user
        lenient().when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid(USER_ID).build());
        payload = new HashMap<>();
        draftStoreEntity = new DraftStoreEntity();
        draftStoreEntity.setId(DRAFT_ID);
        draftStoreEntity.setCaseId(CASE_ID);
        draftStoreEntity.setUserId(USER_ID);
        draftStoreEntity.setPayload(new HashMap<>());
    }

    @Test
    void shouldReturnCreatedWhenNewDraftIsCreated() {
        payload.put("deadline", OffsetDateTime.now());
        when(draftClaimService.createDraftClaim(USER_ID, payload)).thenReturn(draftStoreEntity);

        DraftClaimRequest request = new DraftClaimRequest(null, payload);

        ResponseEntity<DraftClaimResponse> response = controller.createDraftClaim(AUTH, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDraftId()).isEqualTo(DRAFT_ID);
        verify(draftClaimService).createDraftClaim(USER_ID, payload);
    }

    @Test
    void shouldRejectCreateWhenCaseIdIsProvided() {
        DraftClaimRequest request = new DraftClaimRequest(CASE_ID, payload);

        assertThatThrownBy(() -> controller.createDraftClaim(AUTH, request))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(draftClaimService);
    }

    @Test
    void shouldReturnConflictWhenDraftAlreadyExists() {
        ResponseEntity<String> response =
            controller.draftClaimAlreadyExists(new DraftClaimAlreadyExistsException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturnDraftForCaseWhenItExists() {
        when(draftClaimService.getDraftClaimForCase(USER_ID, CASE_ID)).thenReturn(Optional.of(draftStoreEntity));

        ResponseEntity<DraftClaimResponse> response = controller.getDraftClaimForCase(CASE_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCaseId()).isEqualTo(CASE_ID);
    }

    @Test
    void shouldThrowNotFoundWhenNoDraftExistsForCase() {
        when(draftClaimService.getDraftClaimForCase(USER_ID, CASE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getDraftClaimForCase(CASE_ID, AUTH))
            .isInstanceOf(DraftClaimNotFoundException.class);
    }

    @Test
    void shouldReturnActiveDraftWhenItExists() {
        when(draftClaimService.getActiveDraftClaimForUser(USER_ID)).thenReturn(Optional.of(draftStoreEntity));

        ResponseEntity<DraftClaimResponse> response =
            controller.getActiveDraftClaim(AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(draftClaimService).getActiveDraftClaimForUser(USER_ID);
    }

    @Test
    void shouldReturnDraftWhenItExists() {
        when(draftClaimService.getDraftClaim(DRAFT_ID, USER_ID)).thenReturn(Optional.of(draftStoreEntity));

        ResponseEntity<DraftClaimResponse> response = controller.getDraftClaim(DRAFT_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(draftClaimService).getDraftClaim(DRAFT_ID, USER_ID);
    }

    @Test
    void shouldReturnUpdatedDraftWhenDraftExists() {
        DraftClaimRequest request = new DraftClaimRequest(CASE_ID, payload);
        when(draftClaimService.updateDraftClaim(DRAFT_ID, USER_ID, CASE_ID, payload)).thenReturn(draftStoreEntity);

        ResponseEntity<DraftClaimResponse> response = controller.updateDraftClaim(DRAFT_ID, AUTH, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(draftClaimService).updateDraftClaim(DRAFT_ID, USER_ID, CASE_ID, payload);
    }

    @Test
    void shouldReturnNoContentWhenDraftIsDeleted() {
        ResponseEntity<Void> response = controller.deleteDraftClaim(DRAFT_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(draftClaimService).deleteDraftClaim(DRAFT_ID, USER_ID);
    }
}
