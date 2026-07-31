package uk.gov.hmcts.reform.draftstore.services;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftStoreServiceTest {

    private static final String USER_ID = "user1";
    private static final String CASE_ID = "ccd1";
    private static final String NEW_CASE_ID = "ccd2";
    private static final UUID DRAFT_ID = UUID.randomUUID();
    private static final DraftType DRAFT_TYPE = DraftType.DRAFT_CLAIM;

    @Mock
    private DraftStoreRepository draftStoreRepository;

    @InjectMocks
    private DraftStoreService draftStoreService;

    @Nested
    class CreateDraftTests {

        @Test
        void shouldCreateDraftWithExpiryWhenRequestIsValid() {
            Map<String, Object> payload = new HashMap<>(Map.of("step", "claimant-details"));
            when(draftStoreRepository.save(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            DraftStoreEntity result = draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE);

            ArgumentCaptor<DraftStoreEntity> captor = ArgumentCaptor.forClass(DraftStoreEntity.class);
            verify(draftStoreRepository).save(captor.capture());
            DraftStoreEntity savedDraft = captor.getValue();
            assertThat(result).isSameAs(savedDraft);
            assertThat(savedDraft.getId()).isNotNull();
            assertThat(savedDraft.getUserId()).isEqualTo(USER_ID);
            assertThat(savedDraft.getCaseId()).isEqualTo(CASE_ID);
            assertThat(savedDraft.getDraftTypeId()).isEqualTo(DRAFT_TYPE.getId());
            assertThat(savedDraft.getPayload()).isEqualTo(payload).isNotSameAs(payload);
            assertThat(savedDraft.getCreatedAt()).isNotNull();
            assertThat(savedDraft.getUpdatedAt()).isEqualTo(savedDraft.getCreatedAt());
            assertThat(savedDraft.getExpiresAt()).isEqualTo(DRAFT_TYPE.calculateExpiry(savedDraft.getCreatedAt()));
        }

        @Test
        void shouldRejectCreationWhenUserIdIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.createDraft(null, CASE_ID, Map.of(), DRAFT_TYPE))
                .withMessage("userId must not be null");

            verifyNoInteractions(draftStoreRepository);
        }

        @Test
        void shouldRejectCreationWhenPayloadIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.createDraft(USER_ID, CASE_ID, null, DRAFT_TYPE))
                .withMessage("payload must not be null");

            verifyNoInteractions(draftStoreRepository);
        }

        @Test
        void shouldRejectCreationWhenDraftTypeIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.createDraft(USER_ID, CASE_ID, Map.of(), null))
                .withMessage("draftType must not be null");

            verifyNoInteractions(draftStoreRepository);
        }
    }

    @Nested
    class GetDraftTests {

        @Test
        void shouldReturnDraftsWhenUserHasDraftsOfRequestedType() {
            DraftStoreEntity draft = draft();
            when(draftStoreRepository.findByUserIdAndDraftTypeId(USER_ID, DRAFT_TYPE.getId()))
                .thenReturn(List.of(draft));

            List<DraftStoreEntity> result = draftStoreService.getDraftsForUser(USER_ID, DRAFT_TYPE);

            assertThat(result).containsExactly(draft);
            verify(draftStoreRepository).findByUserIdAndDraftTypeId(USER_ID, DRAFT_TYPE.getId());
        }

        @Test
        void shouldReturnActiveDraftsWhenUnexpiredDraftsExist() {
            DraftStoreEntity draft = draft();
            when(draftStoreRepository.findByUserIdAndDraftTypeIdAndExpiresAtAfter(
                eq(USER_ID),
                eq(DRAFT_TYPE.getId()),
                any(OffsetDateTime.class)
            )).thenReturn(List.of(draft));

            List<DraftStoreEntity> result = draftStoreService.getActiveDraftsForUser(USER_ID, DRAFT_TYPE);

            assertThat(result).containsExactly(draft);
            verify(draftStoreRepository).findByUserIdAndDraftTypeIdAndExpiresAtAfter(
                eq(USER_ID),
                eq(DRAFT_TYPE.getId()),
                any(OffsetDateTime.class)
            );
        }

        @Test
        void shouldReturnDraftWhenOwnedUnexpiredDraftExists() {
            DraftStoreEntity draft = draft();
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeIdAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE.getId()),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.of(draft));

            Optional<DraftStoreEntity> result = draftStoreService.getDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);

            assertThat(result).contains(draft);
        }

        @Test
        void shouldRejectLookupWhenDraftIdIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.getDraft(null, USER_ID, DRAFT_TYPE))
                .withMessage("draftId must not be null");

            verifyNoInteractions(draftStoreRepository);
        }
    }

    @Nested
    class UpdateDraftTests {

        @Test
        void shouldUpdateDraftWhenOwnedUnexpiredDraftExists() {
            DraftStoreEntity existingDraft = draft();
            Map<String, Object> payload = new HashMap<>(Map.of("step", "updated"));
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeIdAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE.getId()),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.of(existingDraft));
            when(draftStoreRepository.save(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            Optional<DraftStoreEntity> result = draftStoreService.updateDraft(
                DRAFT_ID,
                USER_ID,
                NEW_CASE_ID,
                payload,
                DRAFT_TYPE
            );

            assertThat(result).contains(existingDraft);
            assertThat(existingDraft.getCaseId()).isEqualTo(NEW_CASE_ID);
            assertThat(existingDraft.getPayload()).isEqualTo(payload).isNotSameAs(payload);
            assertThat(existingDraft.getUpdatedAt()).isNotNull();
            verify(draftStoreRepository).save(existingDraft);
        }

        @Test
        void shouldKeepCaseIdWhenUpdateCaseIdIsNull() {
            DraftStoreEntity existingDraft = draft();
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeIdAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE.getId()),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.of(existingDraft));
            when(draftStoreRepository.save(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            Optional<DraftStoreEntity> result = draftStoreService.updateDraft(
                DRAFT_ID,
                USER_ID,
                null,
                Map.of("step", "updated"),
                DRAFT_TYPE
            );

            assertThat(result).contains(existingDraft);
            assertThat(existingDraft.getCaseId()).isEqualTo(CASE_ID);
        }

        @Test
        void shouldReturnEmptyWhenUpdatingMissingDraft() {
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeIdAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE.getId()),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.empty());

            Optional<DraftStoreEntity> result = draftStoreService.updateDraft(
                DRAFT_ID,
                USER_ID,
                CASE_ID,
                Map.of(),
                DRAFT_TYPE
            );

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class DeleteDraftTests {

        @Test
        void shouldReturnTrueWhenDraftIsDeleted() {
            when(draftStoreRepository.deleteByIdAndUserIdAndDraftTypeId(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE.getId()
            )).thenReturn(1L);

            boolean result = draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);

            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalseWhenDraftDoesNotExist() {
            when(draftStoreRepository.deleteByIdAndUserIdAndDraftTypeId(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE.getId()
            )).thenReturn(0L);

            boolean result = draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);

            assertThat(result).isFalse();
        }

        @Test
        void shouldDeleteAndFlushWhenReplacingExpiredDraft() {
            DraftStoreEntity draft = draft();

            draftStoreService.deleteDraftAndFlush(draft);

            verify(draftStoreRepository).delete(draft);
            verify(draftStoreRepository).flush();
        }
    }

    private DraftStoreEntity draft() {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        return new DraftStoreEntity(
            DRAFT_ID,
            USER_ID,
            CASE_ID,
            DRAFT_TYPE.getId(),
            new HashMap<>(Map.of("step", "existing")),
            createdAt,
            createdAt,
            DRAFT_TYPE.calculateExpiry(createdAt)
        );
    }
}
