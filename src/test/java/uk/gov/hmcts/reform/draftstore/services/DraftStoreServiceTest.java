package uk.gov.hmcts.reform.draftstore.services;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Mock
    private DraftStoreTransactionService draftStoreTransactionService;

    @InjectMocks
    private DraftStoreService draftStoreService;

    @Nested
    class CreateDraftTests {

        @Test
        void shouldCreateDraftWithExpiryWhenRequestIsValid() {
            Map<String, Object> payload = new HashMap<>(Map.of("step", "claimant-details", "draftClaimCacheTtlDays", 14L));
            when(draftStoreTransactionService.saveInNewTransaction(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            DraftStoreEntity result = draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE);

            ArgumentCaptor<DraftStoreEntity> captor = ArgumentCaptor.forClass(DraftStoreEntity.class);
            verify(draftStoreTransactionService).saveInNewTransaction(captor.capture());
            DraftStoreEntity savedDraft = captor.getValue();
            assertThat(result).isSameAs(savedDraft);
            assertThat(savedDraft.getId()).isNotNull();
            assertThat(savedDraft.getUserId()).isEqualTo(USER_ID);
            assertThat(savedDraft.getCaseId()).isEqualTo(CASE_ID);
            assertThat(savedDraft.getDraftType()).isEqualTo(DRAFT_TYPE);
            assertThat(savedDraft.getPayload()).isEqualTo(payload).isNotSameAs(payload);
            assertThat(savedDraft.getCreatedAt()).isNotNull();
            assertThat(savedDraft.getUpdatedAt()).isEqualTo(savedDraft.getCreatedAt());
            assertThat(savedDraft.getExpiresAt()).isEqualTo(savedDraft.getCreatedAt().plusDays(14));
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.draftstore.services.DraftStoreServiceTest#validTtlValues")
        void shouldCalculateExpiryFromPayloadTtl(Object ttlValue, long expectedDays) {
            Map<String, Object> payload = new HashMap<>(Map.of("draftClaimCacheTtlDays", ttlValue));
            when(draftStoreTransactionService.saveInNewTransaction(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            DraftStoreEntity result = draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE);

            assertThat(result.getExpiresAt()).isEqualTo(result.getCreatedAt().plusDays(expectedDays));
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.draftstore.services.DraftStoreServiceTest#invalidTtlValues")
        void shouldDefaultExpiryTo30DaysWhenPayloadTtlIsMissingOrInvalid(Map<String, Object> payload) {
            when(draftStoreTransactionService.saveInNewTransaction(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            DraftStoreEntity result = draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE);

            assertThat(result.getExpiresAt()).isEqualTo(result.getCreatedAt().plusDays(30));
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
        void shouldReturnActiveDraftWhenCreateHitsUniqueConstraint() {
            Map<String, Object> payload = new HashMap<>(Map.of("step", "claimant-details"));
            OffsetDateTime createdAt = OffsetDateTime.now();
            DraftStoreEntity existingDraft = new DraftStoreEntity(
                DRAFT_ID,
                USER_ID,
                CASE_ID,
                DRAFT_TYPE,
                new HashMap<>(Map.of("step", "existing-payload")),
                createdAt,
                createdAt,
                createdAt.plusDays(30)
            );
            when(draftStoreRepository.findByUserIdAndDraftTypeAndExpiresAtAfter(
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(List.of(existingDraft));
            when(draftStoreTransactionService.saveInNewTransaction(any()))
                .thenThrow(new DataIntegrityViolationException("uq_draft_store_user_draft_claim"));

            DraftStoreEntity result = draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE);

            assertThat(result).isSameAs(existingDraft);
            assertThat(result.getPayload()).containsEntry("step", "existing-payload");
        }

        @Test
        void shouldRethrowWhenUniqueConstraintFailsAndNoActiveDraftExists() {
            Map<String, Object> payload = new HashMap<>(Map.of("step", "claimant-details"));
            DataIntegrityViolationException uniqueViolation =
                new DataIntegrityViolationException("uq_draft_store_user_draft_claim");
            when(draftStoreTransactionService.saveInNewTransaction(any(DraftStoreEntity.class))).thenThrow(uniqueViolation);
            when(draftStoreRepository.findByUserIdAndDraftTypeAndExpiresAtAfter(
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(List.of());

            assertThatThrownBy(() -> draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE))
                .isSameAs(uniqueViolation);
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
            when(draftStoreRepository.findByUserIdAndDraftType(USER_ID, DRAFT_TYPE))
                .thenReturn(List.of(draft));

            List<DraftStoreEntity> result = draftStoreService.getDraftsForUser(USER_ID, DRAFT_TYPE);

            assertThat(result).containsExactly(draft);
            verify(draftStoreRepository).findByUserIdAndDraftType(USER_ID, DRAFT_TYPE);
        }

        @Test
        void shouldReturnActiveDraftsWhenUnexpiredDraftsExist() {
            DraftStoreEntity draft = draft();
            when(draftStoreRepository.findByUserIdAndDraftTypeAndExpiresAtAfter(
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(List.of(draft));

            List<DraftStoreEntity> result = draftStoreService.getActiveDraftsForUser(USER_ID, DRAFT_TYPE);

            assertThat(result).containsExactly(draft);
            verify(draftStoreRepository).findByUserIdAndDraftTypeAndExpiresAtAfter(
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            );
        }

        @Test
        void shouldReturnDraftWhenOwnedUnexpiredDraftExists() {
            DraftStoreEntity draft = draft();
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
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
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.of(existingDraft));
            when(draftStoreTransactionService.saveInNewTransaction(any(DraftStoreEntity.class)))
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
            verify(draftStoreTransactionService).saveInNewTransaction(existingDraft);
        }

        @Test
        void shouldKeepCaseIdWhenUpdateCaseIdIsNull() {
            DraftStoreEntity existingDraft = draft();
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.of(existingDraft));
            when(draftStoreTransactionService.saveInNewTransaction(any(DraftStoreEntity.class)))
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
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
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
            when(draftStoreTransactionService.deleteByIdInNewTransaction(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE
            )).thenReturn(1L);

            boolean result = draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);

            assertThat(result).isTrue();
            verify(draftStoreTransactionService).deleteByIdInNewTransaction(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE);
        }

        @Test
        void shouldReturnFalseWhenDraftDoesNotExist() {
            when(draftStoreTransactionService.deleteByIdInNewTransaction(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE
            )).thenReturn(0L);

            boolean result = draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);

            assertThat(result).isFalse();
            verify(draftStoreTransactionService).deleteByIdInNewTransaction(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE);
        }

        @Test
        void shouldDeleteAndFlushWhenReplacingExpiredDraft() {
            DraftStoreEntity draft = draft();

            draftStoreService.deleteDraftAndFlush(draft);

            verify(draftStoreTransactionService).deleteInNewTransaction(draft);
        }
    }

    static Stream<Arguments> validTtlValues() {
        return Stream.of(
            Arguments.of(7L, 7L),
            Arguments.of(28, 28L),
            Arguments.of("60", 60L)
        );
    }

    static Stream<Arguments> invalidTtlValues() {
        Map<String, Object> nullTtl = new HashMap<>();
        nullTtl.put("draftClaimCacheTtlDays", null);
        return Stream.of(
            Arguments.of(new HashMap<>(Map.of("step", "no-ttl"))),
            Arguments.of(nullTtl),
            Arguments.of(new HashMap<>(Map.of("draftClaimCacheTtlDays", 0L))),
            Arguments.of(new HashMap<>(Map.of("draftClaimCacheTtlDays", -5L))),
            Arguments.of(new HashMap<>(Map.of("draftClaimCacheTtlDays", "not-a-number")))
        );
    }

    private DraftStoreEntity draft() {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        return new DraftStoreEntity(
            DRAFT_ID,
            USER_ID,
            CASE_ID,
            DRAFT_TYPE,
            new HashMap<>(Map.of("step", "existing")),
            createdAt,
            createdAt,
            createdAt.plusDays(30)
        );
    }
}
