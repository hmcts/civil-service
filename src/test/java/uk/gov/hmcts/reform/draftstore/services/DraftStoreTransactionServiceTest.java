package uk.gov.hmcts.reform.draftstore.services;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftStoreTransactionServiceTest {

    @Mock
    private DraftStoreRepository draftStoreRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private DraftStoreTransactionService draftStoreTransactionService;

    @Test
    void shouldJoinTransactionBeforeSaveAndFlush() {
        DraftStoreEntity draft = draft();
        when(draftStoreRepository.saveAndFlush(draft)).thenReturn(draft);

        DraftStoreEntity result = draftStoreTransactionService.saveInNewTransaction(draft);

        assertThat(result).isSameAs(draft);
        InOrder order = inOrder(entityManager, draftStoreRepository);
        order.verify(entityManager).joinTransaction();
        order.verify(draftStoreRepository).saveAndFlush(draft);
    }

    @Test
    void shouldPropagateUniqueViolationFromSave() {
        DraftStoreEntity draft = draft();
        DataIntegrityViolationException violation = new DataIntegrityViolationException("duplicate");
        when(draftStoreRepository.saveAndFlush(draft)).thenThrow(violation);

        assertThatThrownBy(() -> draftStoreTransactionService.saveInNewTransaction(draft))
            .isSameAs(violation);
    }

    @Test
    void shouldJoinTransactionBeforeDeleteAndFlush() {
        DraftStoreEntity draft = draft();

        draftStoreTransactionService.deleteInNewTransaction(draft);

        InOrder order = inOrder(entityManager, draftStoreRepository);
        order.verify(entityManager).joinTransaction();
        order.verify(draftStoreRepository).delete(draft);
        order.verify(draftStoreRepository).flush();
    }

    @Test
    void shouldJoinTransactionBeforeDeleteByIdAndFlush() {
        UUID draftId = UUID.randomUUID();
        when(draftStoreRepository.deleteByIdAndUserIdAndDraftType(
            draftId, "user", DraftType.DRAFT_CLAIM))
            .thenReturn(1L);

        long deleted = draftStoreTransactionService.deleteByIdInNewTransaction(
            draftId, "user", DraftType.DRAFT_CLAIM);

        assertThat(deleted).isEqualTo(1L);
        InOrder order = inOrder(entityManager, draftStoreRepository);
        order.verify(entityManager).joinTransaction();
        order.verify(draftStoreRepository).deleteByIdAndUserIdAndDraftType(
            draftId, "user", DraftType.DRAFT_CLAIM);
        order.verify(draftStoreRepository).flush();
    }

    private DraftStoreEntity draft() {
        OffsetDateTime now = OffsetDateTime.now();
        return new DraftStoreEntity(UUID.randomUUID(), "user", null, DraftType.DRAFT_CLAIM,
                                    new HashMap<>(), now, now, now.plusDays(30));
    }
}
