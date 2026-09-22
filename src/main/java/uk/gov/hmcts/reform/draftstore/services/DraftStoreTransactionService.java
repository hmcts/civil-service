package uk.gov.hmcts.reform.draftstore.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

@Service
public class DraftStoreTransactionService {

    private final DraftStoreRepository draftStoreRepository;

    public DraftStoreTransactionService(DraftStoreRepository draftStoreRepository) {
        this.draftStoreRepository = draftStoreRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DraftStoreEntity saveInNewTransaction(DraftStoreEntity draft) {
        return draftStoreRepository.saveAndFlush(draft);
    }
}
