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
import uk.gov.hmcts.reform.dashboard.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.dashboard.services.DraftStoreService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftClaimControllerTest {

    private static final String USER_ID = "USER1";
    private static final String CASE_ID = "CCD123";
    private static final UUID DRAFT_ID = UUID.randomUUID();
    private static final String AUTH = "Token";
    @Mock
    private DraftStoreService draftStoreService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DraftClaimController controller;

    private DraftStoreEntity draftStoreEntity;
    private Map<String, Object> payload;

    @BeforeEach
    void init() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid(USER_ID).build());
        payload = new HashMap<>();
        draftStoreEntity = new DraftStoreEntity();
        draftStoreEntity.setId(DRAFT_ID);
        draftStoreEntity.setCaseId(CASE_ID);
        draftStoreEntity.setUserId(USER_ID);
        draftStoreEntity.setPayload(new HashMap<>());
    }

    @Test
    void shouldCreateDraftClaim() {
        payload.put("deadline", OffsetDateTime.now());
        when(draftStoreService.createDraftClaim(USER_ID, CASE_ID, payload)).thenReturn(draftStoreEntity);

        DraftClaimRequest request = new DraftClaimRequest(CASE_ID, payload);

        ResponseEntity<DraftClaimResponse> response = controller.createDraftClaim(AUTH, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        verify(draftStoreService).createDraftClaim(USER_ID, CASE_ID, payload);
    }

    @Test
    void shouldReturnActiveDraftClaim() {
        when(draftStoreService.getActiveDraftClaimForUser(USER_ID)).thenReturn(Optional.of(draftStoreEntity));

        ResponseEntity<DraftClaimResponse> response =
            controller.getActiveDraftClaim(AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(draftStoreService).getActiveDraftClaimForUser(USER_ID);
    }

    @Test
    void shouldReturnDraftClaim() {
        when(draftStoreService.getDraftClaim(DRAFT_ID, USER_ID)).thenReturn(Optional.of(draftStoreEntity));

        ResponseEntity<DraftClaimResponse> response = controller.getDraftClaim(DRAFT_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(draftStoreService).getDraftClaim(DRAFT_ID, USER_ID);
    }

    @Test
    void shouldUpdateDraftClaim() {
        DraftClaimRequest request = new DraftClaimRequest(CASE_ID, payload);
        when(draftStoreService.updateDraftClaim(DRAFT_ID, USER_ID, CASE_ID, payload)).thenReturn(draftStoreEntity);

        ResponseEntity<DraftClaimResponse> response = controller.updateDraftClaim(DRAFT_ID, AUTH, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(draftStoreService).updateDraftClaim(DRAFT_ID, USER_ID, CASE_ID, payload);
    }

    @Test
    void shouldDeleteDraftClaim() {
        ResponseEntity<Void> response = controller.deleteDraftClaim(DRAFT_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(draftStoreService).deleteDraftClaim(DRAFT_ID, USER_ID);
    }
}
