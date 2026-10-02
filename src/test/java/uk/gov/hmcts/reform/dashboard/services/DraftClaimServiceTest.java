package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimNotFoundException;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftClaimServiceTest {

    private static final String USER_ID = "user1";
    private static final String CASE_ID = "ccd1";
    private static final String NEW_CASE_ID = "ccd2";
    private static final UUID DRAFT_ID = UUID.randomUUID();
    private static final DraftType DRAFT_TYPE = DraftType.DRAFT_CLAIM;
    private static final long RETENTION_DAYS = 30L;

    @Mock
    private DraftStoreService draftStoreService;

    @InjectMocks
    private DraftClaimService draftClaimService;

    @Nested
    class CreateDraftClaimTests {

        @Test
        void shouldCreateBlankDraftWhenUserHasNoBlankDraft() {
            Map<String, Object> payload = Map.of("step", "claimant-details");
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity createdDraft = draft(null, createdAt, createdAt.plusDays(RETENTION_DAYS));
            when(draftStoreService.getBlankDraft(USER_ID, DRAFT_TYPE)).thenReturn(Optional.empty());
            when(draftStoreService.createDraft(USER_ID, null, payload, DRAFT_TYPE)).thenReturn(createdDraft);

            DraftStoreEntity result = draftClaimService.createDraftClaim(USER_ID, payload);

            assertThat(result).isSameAs(createdDraft);
            verify(draftStoreService).createDraft(USER_ID, null, payload, DRAFT_TYPE);
        }

        @Test
        void shouldRejectCreationWhenActiveBlankDraftExists() {
            OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
            DraftStoreEntity existingBlankDraft = draft(null, createdAt, createdAt.plusDays(RETENTION_DAYS));
            when(draftStoreService.getBlankDraft(USER_ID, DRAFT_TYPE)).thenReturn(Optional.of(existingBlankDraft));

            assertThatThrownBy(() -> draftClaimService.createDraftClaim(USER_ID, Map.of("step", "new")))
                .isInstanceOf(DraftClaimAlreadyExistsException.class);

            verify(draftStoreService).getBlankDraft(USER_ID, DRAFT_TYPE);
            verifyNoMoreInteractions(draftStoreService);
        }

        @Test
        void shouldReplaceExpiredBlankDraftWithNewDraft() {
            OffsetDateTime createdAt = OffsetDateTime.now().minusDays(RETENTION_DAYS + 1);
            DraftStoreEntity expiredDraft = draft(null, createdAt, createdAt.plusDays(RETENTION_DAYS));
            OffsetDateTime newCreatedAt = OffsetDateTime.now();
            DraftStoreEntity newDraft = draft(null, newCreatedAt, newCreatedAt.plusDays(RETENTION_DAYS));
            Map<String, Object> payload = Map.of("step", "new-payload");
            when(draftStoreService.getBlankDraft(USER_ID, DRAFT_TYPE)).thenReturn(Optional.of(expiredDraft));
            when(draftStoreService.createDraft(USER_ID, null, payload, DRAFT_TYPE)).thenReturn(newDraft);

            DraftStoreEntity result = draftClaimService.createDraftClaim(USER_ID, payload);

            assertThat(result).isSameAs(newDraft);
            verify(draftStoreService).deleteDraftAndFlush(expiredDraft);
            verify(draftStoreService).createDraft(USER_ID, null, payload, DRAFT_TYPE);
        }

        @Test
        void shouldRejectCreationWhenConcurrentRequestCreatesBlankDraftFirst() {
            Map<String, Object> payload = Map.of("step", "claimant-details");
            DataIntegrityViolationException uniqueViolation =
                new DataIntegrityViolationException("uq_draft_store_active_draft");
            when(draftStoreService.getBlankDraft(USER_ID, DRAFT_TYPE)).thenReturn(Optional.empty());
            when(draftStoreService.createDraft(USER_ID, null, payload, DRAFT_TYPE)).thenThrow(uniqueViolation);

            assertThatThrownBy(() -> draftClaimService.createDraftClaim(USER_ID, payload))
                .isInstanceOf(DraftClaimAlreadyExistsException.class)
                .hasCause(uniqueViolation);
        }

        @Test
        void shouldRejectCreationWhenUserIdIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftClaimService.createDraftClaim(null, Map.of()))
                .withMessage("userId must not be null");

            verifyNoInteractions(draftStoreService);
        }

        @Test
        void shouldRejectCreationWhenPayloadIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftClaimService.createDraftClaim(USER_ID, null))
                .withMessage("payload must not be null");

            verifyNoInteractions(draftStoreService);
        }
    }

    @Nested
    class GetDraftClaimTests {

        @Test
        void shouldReturnDraftWhenDraftExists() {
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity draft = draft(createdAt, createdAt.plusDays(RETENTION_DAYS));
            when(draftStoreService.getDraft(DRAFT_ID, USER_ID, DRAFT_TYPE)).thenReturn(Optional.of(draft));

            Optional<DraftStoreEntity> result = draftClaimService.getDraftClaim(DRAFT_ID, USER_ID);

            assertThat(result).contains(draft);
            verify(draftStoreService).getDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);
        }

        @Test
        void shouldReturnActiveBlankDraft() {
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity blankDraft = draft(null, createdAt, createdAt.plusDays(RETENTION_DAYS));
            when(draftStoreService.getActiveBlankDraft(USER_ID, DRAFT_TYPE)).thenReturn(Optional.of(blankDraft));

            Optional<DraftStoreEntity> result = draftClaimService.getActiveDraftClaimForUser(USER_ID);

            assertThat(result).contains(blankDraft);
        }

        @Test
        void shouldReturnEmptyWhenNoActiveBlankDraftExists() {
            when(draftStoreService.getActiveBlankDraft(USER_ID, DRAFT_TYPE)).thenReturn(Optional.empty());

            Optional<DraftStoreEntity> result = draftClaimService.getActiveDraftClaimForUser(USER_ID);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnActiveDraftForCase() {
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity caseDraft = draft(CASE_ID, createdAt, createdAt.plusDays(RETENTION_DAYS));
            when(draftStoreService.getActiveDraftForCase(USER_ID, CASE_ID, DRAFT_TYPE)).thenReturn(Optional.of(caseDraft));

            Optional<DraftStoreEntity> result = draftClaimService.getDraftClaimForCase(USER_ID, CASE_ID);

            assertThat(result).contains(caseDraft);
        }

        @Test
        void shouldReturnEmptyWhenNoActiveDraftExistsForCase() {
            when(draftStoreService.getActiveDraftForCase(USER_ID, CASE_ID, DRAFT_TYPE)).thenReturn(Optional.empty());

            Optional<DraftStoreEntity> result = draftClaimService.getDraftClaimForCase(USER_ID, CASE_ID);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldTrimCaseIdWhenLookingUpDraftForCase() {
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity caseDraft = draft(CASE_ID, createdAt, createdAt.plusDays(RETENTION_DAYS));
            when(draftStoreService.getActiveDraftForCase(USER_ID, CASE_ID, DRAFT_TYPE)).thenReturn(Optional.of(caseDraft));

            Optional<DraftStoreEntity> result = draftClaimService.getDraftClaimForCase(USER_ID, "  " + CASE_ID + " ");

            assertThat(result).contains(caseDraft);
        }

        @Test
        void shouldReturnEmptyWithoutQueryingWhenCaseIdIsBlank() {
            Optional<DraftStoreEntity> result = draftClaimService.getDraftClaimForCase(USER_ID, " ");

            assertThat(result).isEmpty();
            verifyNoInteractions(draftStoreService);
        }
    }

    @Nested
    class UpdateDraftClaimTests {

        @Test
        void shouldReturnUpdatedDraftWhenDraftExists() {
            Map<String, Object> payload = Map.of("step", "updated");
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity updatedDraft = draft(createdAt, createdAt.plusDays(RETENTION_DAYS));
            when(draftStoreService.updateDraft(DRAFT_ID, USER_ID, NEW_CASE_ID, payload, DRAFT_TYPE))
                .thenReturn(Optional.of(updatedDraft));

            DraftStoreEntity result = draftClaimService.updateDraftClaim(
                DRAFT_ID,
                USER_ID,
                NEW_CASE_ID,
                payload
            );

            assertThat(result).isSameAs(updatedDraft);
            verify(draftStoreService).updateDraft(DRAFT_ID, USER_ID, NEW_CASE_ID, payload, DRAFT_TYPE);
        }

        @Test
        void shouldTrimCaseIdWhenUpdatingDraft() {
            Map<String, Object> payload = Map.of("step", "submitted");
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity updatedDraft = draft(createdAt, createdAt.plusDays(RETENTION_DAYS));
            when(draftStoreService.updateDraft(DRAFT_ID, USER_ID, CASE_ID, payload, DRAFT_TYPE))
                .thenReturn(Optional.of(updatedDraft));

            draftClaimService.updateDraftClaim(DRAFT_ID, USER_ID, " " + CASE_ID + "  ", payload);

            verify(draftStoreService).updateDraft(DRAFT_ID, USER_ID, CASE_ID, payload, DRAFT_TYPE);
        }

        @Test
        void shouldNotOverwriteCaseIdWhenUpdateCaseIdIsBlank() {
            Map<String, Object> payload = Map.of("step", "updated");
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity updatedDraft = draft(createdAt, createdAt.plusDays(RETENTION_DAYS));
            when(draftStoreService.updateDraft(DRAFT_ID, USER_ID, null, payload, DRAFT_TYPE))
                .thenReturn(Optional.of(updatedDraft));

            draftClaimService.updateDraftClaim(DRAFT_ID, USER_ID, "", payload);

            verify(draftStoreService).updateDraft(DRAFT_ID, USER_ID, null, payload, DRAFT_TYPE);
        }

        @Test
        void shouldThrowNotFoundWhenUpdatingMissingDraft() {
            Map<String, Object> payload = Map.of("step", "updated");
            when(draftStoreService.updateDraft(DRAFT_ID, USER_ID, CASE_ID, payload, DRAFT_TYPE))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> draftClaimService.updateDraftClaim(DRAFT_ID, USER_ID, CASE_ID, payload))
                .isInstanceOf(DraftClaimNotFoundException.class);
        }
    }

    @Nested
    class DeleteDraftClaimTests {

        @Test
        void shouldDeleteDraftWhenDraftExists() {
            when(draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE)).thenReturn(true);

            draftClaimService.deleteDraftClaim(DRAFT_ID, USER_ID);

            verify(draftStoreService).deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);
        }

        @Test
        void shouldThrowNotFoundWhenDeletingMissingDraft() {
            when(draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE)).thenReturn(false);

            assertThatThrownBy(() -> draftClaimService.deleteDraftClaim(DRAFT_ID, USER_ID))
                .isInstanceOf(DraftClaimNotFoundException.class);
        }
    }

    private DraftStoreEntity draft(OffsetDateTime createdAt, OffsetDateTime expiresAt) {
        return draft(CASE_ID, createdAt, expiresAt);
    }

    private DraftStoreEntity draft(String caseId, OffsetDateTime createdAt, OffsetDateTime expiresAt) {
        return new DraftStoreEntity(
            DRAFT_ID,
            USER_ID,
            caseId,
            DRAFT_TYPE,
            new HashMap<>(Map.of("step", "existing-payload")),
            createdAt,
            createdAt,
            expiresAt
        );
    }
}
