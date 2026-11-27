package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftGeneralApplicationAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftGeneralApplicationNotFoundException;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftGeneralApplicationServiceTest {

    private static final String USER_ID = "user-1";
    private static final String CASE_ID = "1234567890123456";
    private static final String APPLICATION_ID = "9876543210987654";
    private static final UUID DRAFT_ID = UUID.randomUUID();

    @Mock
    private DraftStoreService draftStoreService;

    @InjectMocks
    private DraftGeneralApplicationService service;

    @Test
    void shouldCreateApplicationDraftWhenNoneExists() {
        Map<String, Object> payload = new HashMap<>(Map.of("generalApplication", Map.of("step", "type")));
        DraftStoreEntity created = draft(null, OffsetDateTime.now().plusDays(180));
        when(draftStoreService.getDraftForCase(USER_ID, CASE_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(Optional.empty());
        when(draftStoreService.createDraft(
            eq(USER_ID), eq(CASE_ID), isNull(), eq(payload), eq(DraftType.DRAFT_GENERAL_APPLICATION)
        )).thenReturn(created);

        DraftStoreEntity result = service.create(USER_ID, CASE_ID, null, payload);

        assertThat(result).isEqualTo(created);
    }

    @Test
    void shouldKeepResponseDraftsForTheSameCaseSeparate() {
        DraftStoreEntity first = draft(APPLICATION_ID, OffsetDateTime.now().plusDays(10));
        DraftStoreEntity second = draft("1111222233334444", OffsetDateTime.now().plusDays(10));
        when(draftStoreService.listActiveDraftsForCase(USER_ID, CASE_ID, DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE))
            .thenReturn(List.of(first, second));

        List<DraftStoreEntity> responses = service.getResponsesForCase(USER_ID, CASE_ID);

        assertThat(responses).containsExactly(first, second);
    }

    @Test
    void shouldRejectASecondActiveApplicationDraftForTheSameCase() {
        when(draftStoreService.getDraftForCase(USER_ID, CASE_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(Optional.of(draft(null, OffsetDateTime.now().plusDays(1))));

        assertThatThrownBy(() -> service.create(USER_ID, CASE_ID, "  ", Map.of("step", "again")))
            .isInstanceOf(DraftGeneralApplicationAlreadyExistsException.class);
        verify(draftStoreService, never()).createDraft(any(), any(), any(), any(), any());
    }

    @Test
    void shouldReplaceAnExpiredApplicationDraft() {
        DraftStoreEntity expired = draft(null, OffsetDateTime.now().minusDays(1));
        Map<String, Object> payload = Map.of("generalApplication", Map.of("step", "restart"));
        when(draftStoreService.getDraftForCase(USER_ID, CASE_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(Optional.of(expired));
        when(draftStoreService.createDraft(
            eq(USER_ID), eq(CASE_ID), isNull(), eq(payload), eq(DraftType.DRAFT_GENERAL_APPLICATION)
        )).thenReturn(draft(null, OffsetDateTime.now().plusDays(180)));

        service.create(USER_ID, CASE_ID, null, payload);

        verify(draftStoreService).deleteDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION);
    }

    @Test
    void shouldNotReturnAnExpiredResponse() {
        when(draftStoreService.getActiveDraftForApplication(
            USER_ID, APPLICATION_ID, DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE
        )).thenReturn(Optional.empty());

        assertThat(service.getResponseForApplication(USER_ID, APPLICATION_ID)).isEmpty();
    }

    @Test
    void shouldReturnTheApplicationDraftWhenItExists() {
        DraftStoreEntity application = draft(null, OffsetDateTime.now().plusDays(1));
        when(draftStoreService.getDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(Optional.of(application));

        assertThat(service.get(DRAFT_ID, USER_ID)).contains(application);
        verify(draftStoreService, never()).getDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE);
    }

    @Test
    void shouldReturnTheActiveApplicationDraftForACase() {
        DraftStoreEntity application = draft(null, OffsetDateTime.now().plusDays(1));
        when(draftStoreService.getActiveDraftForCase(USER_ID, CASE_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(Optional.of(application));

        assertThat(service.getApplicationForCase(USER_ID, CASE_ID)).contains(application);
    }

    @Test
    void shouldIgnoreABlankCaseWhenLookingUpAnApplication() {
        assertThat(service.getApplicationForCase(USER_ID, "  ")).isEmpty();
        verify(draftStoreService, never()).getActiveDraftForCase(any(), any(), any());
    }

    @Test
    void shouldIgnoreABlankCaseWhenListingResponses() {
        assertThat(service.getResponsesForCase(USER_ID, "  ")).isEmpty();
        verify(draftStoreService, never()).listActiveDraftsForCase(any(), any(), any());
    }

    @Test
    void shouldIgnoreABlankApplicationWhenLookingUpAResponse() {
        assertThat(service.getResponseForApplication(USER_ID, "  ")).isEmpty();
        verify(draftStoreService, never()).getActiveDraftForApplication(any(), any(), any());
    }

    @Test
    void shouldUpdateAnExistingDraft() {
        Map<String, Object> payload = Map.of("step", "next");
        DraftStoreEntity existing = draft(null, OffsetDateTime.now().plusDays(1));
        DraftStoreEntity updated = draft(null, OffsetDateTime.now().plusDays(2));
        when(draftStoreService.getDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(Optional.of(existing));
        when(draftStoreService.updateDraft(
            DRAFT_ID, USER_ID, CASE_ID, payload, DraftType.DRAFT_GENERAL_APPLICATION
        )).thenReturn(Optional.of(updated));

        assertThat(service.update(DRAFT_ID, USER_ID, CASE_ID, payload)).isEqualTo(updated);
    }

    @Test
    void shouldThrowNotFoundWhenTheDraftDisappearsDuringUpdate() {
        when(draftStoreService.getDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(Optional.of(draft(null, OffsetDateTime.now().plusDays(1))));
        when(draftStoreService.updateDraft(
            eq(DRAFT_ID), eq(USER_ID), eq(CASE_ID), any(), eq(DraftType.DRAFT_GENERAL_APPLICATION)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(DRAFT_ID, USER_ID, CASE_ID, Map.of("step", "next")))
            .isInstanceOf(DraftGeneralApplicationNotFoundException.class);
    }

    @Test
    void shouldDeleteAnApplicationDraft() {
        when(draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(true);

        service.delete(DRAFT_ID, USER_ID);

        verify(draftStoreService, never()).deleteDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE);
    }

    @Test
    void shouldDeleteAResponseDraftWhenTheApplicationDraftIsMissing() {
        when(draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(false);
        when(draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE))
            .thenReturn(true);

        service.delete(DRAFT_ID, USER_ID);
    }

    @Test
    void shouldThrowNotFoundWhenNeitherDraftCanBeDeleted() {
        when(draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(false);
        when(draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE))
            .thenReturn(false);

        assertThatThrownBy(() -> service.delete(DRAFT_ID, USER_ID))
            .isInstanceOf(DraftGeneralApplicationNotFoundException.class);
    }

    @Test
    void shouldDeleteTheApplicationDraftForACase() {
        DraftStoreEntity application = draft(null, OffsetDateTime.now().plusDays(1));
        when(draftStoreService.getActiveDraftForCase(USER_ID, CASE_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(Optional.of(application));

        service.deleteApplicationForCase(USER_ID, CASE_ID);

        verify(draftStoreService).deleteDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION);
    }

    @Test
    void shouldDoNothingWhenTheCaseHasNoApplicationDraft() {
        when(draftStoreService.getActiveDraftForCase(USER_ID, CASE_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(Optional.empty());

        service.deleteApplicationForCase(USER_ID, CASE_ID);

        verify(draftStoreService, never()).deleteDraft(any(), any(), any());
    }

    @Test
    void shouldDeleteTheResponseDraftForAnApplication() {
        DraftStoreEntity response = draft(APPLICATION_ID, OffsetDateTime.now().plusDays(1));
        when(draftStoreService.getActiveDraftForApplication(
            USER_ID, APPLICATION_ID, DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE
        )).thenReturn(Optional.of(response));

        service.deleteResponseForApplication(USER_ID, APPLICATION_ID);

        verify(draftStoreService).deleteDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE);
    }

    @Test
    void shouldDeleteEveryActiveApplicationDraftForTheUser() {
        DraftStoreEntity application = draft(null, OffsetDateTime.now().plusDays(1));
        when(draftStoreService.listActiveDraftsForUser(USER_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(List.of(application));

        service.deleteApplicationDraftsForUser(USER_ID);

        verify(draftStoreService).deleteDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION);
    }

    @Test
    void shouldRejectABlankCaseId() {
        assertThatThrownBy(() -> service.create(USER_ID, "  ", null, Map.of("step", "type")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("caseId must not be blank");
        verify(draftStoreService, never()).createDraft(any(), any(), any(), any(), any());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingAMissingDraft() {
        when(draftStoreService.getDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION))
            .thenReturn(Optional.empty());
        when(draftStoreService.getDraft(DRAFT_ID, USER_ID, DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(DRAFT_ID, USER_ID, CASE_ID, Map.of("step", "next")))
            .isInstanceOf(DraftGeneralApplicationNotFoundException.class);
    }

    @Test
    void shouldMapADuplicateInsertToAlreadyExists() {
        when(draftStoreService.getDraftForApplication(USER_ID, APPLICATION_ID, DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE))
            .thenReturn(Optional.empty());
        when(draftStoreService.createDraft(
            eq(USER_ID), eq(CASE_ID), eq(APPLICATION_ID), any(), eq(DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE)
        )).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.create(USER_ID, CASE_ID, APPLICATION_ID, Map.of("response", Map.of())))
            .isInstanceOf(DraftGeneralApplicationAlreadyExistsException.class);
    }

    private static DraftStoreEntity draft(String applicationId, OffsetDateTime expiresAt) {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        DraftType draftType = applicationId == null
            ? DraftType.DRAFT_GENERAL_APPLICATION
            : DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE;
        return new DraftStoreEntity(
            DRAFT_ID,
            USER_ID,
            CASE_ID,
            draftType,
            new HashMap<>(Map.of("step", "saved")),
            createdAt,
            createdAt,
            expiresAt,
            null,
            applicationId
        );
    }
}
