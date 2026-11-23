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
import uk.gov.hmcts.reform.dashboard.data.DraftGeneralApplicationCaseResponse;
import uk.gov.hmcts.reform.dashboard.data.DraftGeneralApplicationRequest;
import uk.gov.hmcts.reform.dashboard.data.DraftGeneralApplicationResponse;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftGeneralApplicationAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftGeneralApplicationNotFoundException;
import uk.gov.hmcts.reform.dashboard.services.DraftGeneralApplicationService;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftGeneralApplicationControllerTest {

    private static final String USER_ID = "user-1";
    private static final String CASE_ID = "1234567890123456";
    private static final String APPLICATION_ID = "9876543210987654";
    private static final UUID DRAFT_ID = UUID.randomUUID();
    private static final String AUTH = "Bearer token";

    @Mock
    private DraftGeneralApplicationService draftGeneralApplicationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DraftGeneralApplicationController controller;

    @BeforeEach
    void setUp() {
        lenient().when(userService.getUserInfo(AUTH)).thenReturn(UserInfo.builder().uid(USER_ID).build());
    }

    @Test
    void shouldReturnCreatedApplicationDraft() {
        Map<String, Object> payload = Map.of("generalApplication", Map.of("step", "type"));
        DraftGeneralApplicationRequest request = new DraftGeneralApplicationRequest(CASE_ID, null, payload);
        when(draftGeneralApplicationService.create(USER_ID, CASE_ID, null, payload)).thenReturn(applicationDraft());

        ResponseEntity<DraftGeneralApplicationResponse> response = controller.create(AUTH, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDraftId()).isEqualTo(DRAFT_ID);
        assertThat(response.getBody().getApplicationId()).isNull();
    }

    @Test
    void shouldReturnTheApplicationAndEveryResponseForACase() {
        when(draftGeneralApplicationService.getApplicationForCase(USER_ID, CASE_ID))
            .thenReturn(Optional.of(applicationDraft()));
        when(draftGeneralApplicationService.getResponsesForCase(USER_ID, CASE_ID))
            .thenReturn(List.of(responseDraft(APPLICATION_ID), responseDraft("2222333344445555")));

        ResponseEntity<DraftGeneralApplicationCaseResponse> response = controller.getForCase(CASE_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getApplication().getDraftId()).isEqualTo(DRAFT_ID);
        assertThat(response.getBody().getResponses()).hasSize(2);
    }

    @Test
    void shouldReturnNotFoundWhenTheResponseDraftIsMissing() {
        ResponseEntity<String> response = controller.notFound(
            new DraftGeneralApplicationNotFoundException(APPLICATION_ID)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnConflictWhenADraftAlreadyExists() {
        ResponseEntity<String> response = controller.alreadyExists(
            new DraftGeneralApplicationAlreadyExistsException("caseId " + CASE_ID)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturnTheResponseDraftForAnApplication() {
        when(draftGeneralApplicationService.getResponseForApplication(USER_ID, APPLICATION_ID))
            .thenReturn(Optional.of(responseDraft(APPLICATION_ID)));

        ResponseEntity<DraftGeneralApplicationResponse> response = controller.getForApplication(APPLICATION_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getApplicationId()).isEqualTo(APPLICATION_ID);
    }

    @Test
    void shouldThrowWhenTheResponseDraftIsMissing() {
        when(draftGeneralApplicationService.getResponseForApplication(USER_ID, APPLICATION_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getForApplication(APPLICATION_ID, AUTH))
            .isInstanceOf(DraftGeneralApplicationNotFoundException.class);
    }

    @Test
    void shouldReturnADraftById() {
        when(draftGeneralApplicationService.get(DRAFT_ID, USER_ID)).thenReturn(Optional.of(applicationDraft()));

        ResponseEntity<DraftGeneralApplicationResponse> response = controller.get(DRAFT_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDraftId()).isEqualTo(DRAFT_ID);
    }

    @Test
    void shouldThrowWhenTheDraftIdIsMissing() {
        when(draftGeneralApplicationService.get(DRAFT_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.get(DRAFT_ID, AUTH))
            .isInstanceOf(DraftGeneralApplicationNotFoundException.class);
    }

    @Test
    void shouldReturnTheUpdatedDraft() {
        Map<String, Object> payload = Map.of("generalApplication", Map.of("step", "next"));
        DraftGeneralApplicationRequest request = new DraftGeneralApplicationRequest(CASE_ID, null, payload);
        when(draftGeneralApplicationService.update(DRAFT_ID, USER_ID, CASE_ID, payload))
            .thenReturn(applicationDraft());

        ResponseEntity<DraftGeneralApplicationResponse> response = controller.update(DRAFT_ID, AUTH, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDraftId()).isEqualTo(DRAFT_ID);
    }

    @Test
    void shouldDeleteEveryApplicationDraftForTheUser() {
        ResponseEntity<Void> response = controller.deleteApplicationDraftsForUser(AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(draftGeneralApplicationService).deleteApplicationDraftsForUser(USER_ID);
    }

    @Test
    void shouldDeleteTheResponseDraftForAnApplication() {
        ResponseEntity<Void> response = controller.deleteResponseForApplication(APPLICATION_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(draftGeneralApplicationService).deleteResponseForApplication(USER_ID, APPLICATION_ID);
    }

    @Test
    void shouldDeleteADraftById() {
        ResponseEntity<Void> response = controller.delete(DRAFT_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(draftGeneralApplicationService).delete(DRAFT_ID, USER_ID);
    }

    @Test
    void shouldReturnBadRequestForAnIllegalArgument() {
        ResponseEntity<String> response = controller.badRequest(new IllegalArgumentException("caseId must not be blank"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("caseId must not be blank");
    }

    @Test
    void shouldDeleteOnlyTheApplicationDraftForTheCase() {
        ResponseEntity<Void> response = controller.deleteApplicationForCase(CASE_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(draftGeneralApplicationService).deleteApplicationForCase(USER_ID, CASE_ID);
    }

    private static DraftStoreEntity applicationDraft() {
        return stored(null, DraftType.DRAFT_GENERAL_APPLICATION);
    }

    private static DraftStoreEntity responseDraft(String applicationId) {
        DraftStoreEntity draft = stored(applicationId, DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE);
        draft.setId(UUID.randomUUID());
        return draft;
    }

    private static DraftStoreEntity stored(String applicationId, DraftType draftType) {
        OffsetDateTime now = OffsetDateTime.now();
        return new DraftStoreEntity(
            DRAFT_ID,
            USER_ID,
            CASE_ID,
            draftType,
            new HashMap<>(),
            now,
            now,
            now.plusDays(180),
            null,
            applicationId
        );
    }
}
