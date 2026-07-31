package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimNotFoundException;
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

    @Mock
    private DraftStoreService draftStoreService;

    @InjectMocks
    private DraftClaimService draftClaimService;

    @Nested
    class CreateDraftClaimTests {

        @Test
        void shouldCreateDraftWhenNoExistingDraftIsFound() {
            Map<String, Object> payload = Map.of("step", "claimant-details");
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity createdDraft = draft(createdAt, DRAFT_TYPE.calculateExpiry(createdAt));
            when(draftStoreService.getDraftsForUser(USER_ID, DRAFT_TYPE)).thenReturn(List.of());
            when(draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE)).thenReturn(createdDraft);

            DraftClaimCreationResult result = draftClaimService.createDraftClaim(USER_ID, CASE_ID, payload);

            assertThat(result.newlyCreated()).isTrue();
            assertThat(result.draftClaim()).isSameAs(createdDraft);
            verify(draftStoreService).getDraftsForUser(USER_ID, DRAFT_TYPE);
            verify(draftStoreService).createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE);
        }

        @Test
        void shouldReturnExistingDraftWhenActiveDraftIsFound() {
            OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
            DraftStoreEntity existingDraft = draft(createdAt, DRAFT_TYPE.calculateExpiry(createdAt));
            when(draftStoreService.getDraftsForUser(USER_ID, DRAFT_TYPE)).thenReturn(List.of(existingDraft));

            DraftClaimCreationResult result = draftClaimService.createDraftClaim(
                USER_ID,
                NEW_CASE_ID,
                Map.of("step", "new-payload")
            );

            assertThat(result.newlyCreated()).isFalse();
            assertThat(result.draftClaim()).isSameAs(existingDraft);
            assertThat(existingDraft.getCaseId()).isEqualTo(CASE_ID);
            assertThat(existingDraft.getPayload()).containsEntry("step", "existing-payload");
            verify(draftStoreService).getDraftsForUser(USER_ID, DRAFT_TYPE);
            verifyNoMoreInteractions(draftStoreService);
        }

        @Test
        void shouldReplaceDraftWhenExistingDraftIsExpired() {
            OffsetDateTime createdAt = OffsetDateTime.now().minusDays(181);
            DraftStoreEntity expiredDraft = draft(createdAt, DRAFT_TYPE.calculateExpiry(createdAt));
            OffsetDateTime replacementCreatedAt = OffsetDateTime.now();
            DraftStoreEntity replacementDraft = draft(
                replacementCreatedAt,
                DRAFT_TYPE.calculateExpiry(replacementCreatedAt)
            );
            Map<String, Object> payload = Map.of("step", "new-payload");
            when(draftStoreService.getDraftsForUser(USER_ID, DRAFT_TYPE)).thenReturn(List.of(expiredDraft));
            when(draftStoreService.createDraft(USER_ID, NEW_CASE_ID, payload, DRAFT_TYPE))
                .thenReturn(replacementDraft);

            DraftClaimCreationResult result = draftClaimService.createDraftClaim(
                USER_ID,
                NEW_CASE_ID,
                payload
            );

            assertThat(result.newlyCreated()).isTrue();
            assertThat(result.draftClaim()).isSameAs(replacementDraft);
            verify(draftStoreService).deleteDraftAndFlush(expiredDraft);
            verify(draftStoreService).createDraft(USER_ID, NEW_CASE_ID, payload, DRAFT_TYPE);
        }

        @Test
        void shouldRejectCreationWhenUserIdIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftClaimService.createDraftClaim(null, CASE_ID, Map.of()))
                .withMessage("userId must not be null");

            verifyNoInteractions(draftStoreService);
        }

        @Test
        void shouldRejectCreationWhenPayloadIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftClaimService.createDraftClaim(USER_ID, CASE_ID, null))
                .withMessage("payload must not be null");

            verifyNoInteractions(draftStoreService);
        }
    }

    @Nested
    class GetDraftClaimTests {

        @Test
        void shouldReturnDraftWhenDraftExists() {
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity draft = draft(createdAt, DRAFT_TYPE.calculateExpiry(createdAt));
            when(draftStoreService.getDraft(DRAFT_ID, USER_ID, DRAFT_TYPE)).thenReturn(Optional.of(draft));

            Optional<DraftStoreEntity> result = draftClaimService.getDraftClaim(DRAFT_ID, USER_ID);

            assertThat(result).contains(draft);
            verify(draftStoreService).getDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);
        }

        @Test
        void shouldReturnActiveDraftWhenActiveDraftExists() {
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity draft = draft(createdAt, DRAFT_TYPE.calculateExpiry(createdAt));
            when(draftStoreService.getActiveDraftsForUser(USER_ID, DRAFT_TYPE)).thenReturn(List.of(draft));

            Optional<DraftStoreEntity> result = draftClaimService.getActiveDraftClaimForUser(USER_ID);

            assertThat(result).contains(draft);
            verify(draftStoreService).getActiveDraftsForUser(USER_ID, DRAFT_TYPE);
        }

        @Test
        void shouldReturnEmptyWhenNoActiveDraftExists() {
            when(draftStoreService.getActiveDraftsForUser(USER_ID, DRAFT_TYPE)).thenReturn(List.of());

            Optional<DraftStoreEntity> result = draftClaimService.getActiveDraftClaimForUser(USER_ID);

            assertThat(result).isEmpty();
            verify(draftStoreService).getActiveDraftsForUser(USER_ID, DRAFT_TYPE);
        }
    }

    @Nested
    class UpdateDraftClaimTests {

        @Test
        void shouldReturnUpdatedDraftWhenDraftExists() {
            Map<String, Object> payload = Map.of("step", "updated");
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity updatedDraft = draft(createdAt, DRAFT_TYPE.calculateExpiry(createdAt));
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
        return new DraftStoreEntity(
            DRAFT_ID,
            USER_ID,
            CASE_ID,
            DRAFT_TYPE.getId(),
            new HashMap<>(Map.of("step", "existing-payload")),
            createdAt,
            createdAt,
            expiresAt
        );
    }
}
