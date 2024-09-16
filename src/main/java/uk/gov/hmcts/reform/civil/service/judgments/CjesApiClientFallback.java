package uk.gov.hmcts.reform.civil.service.judgments;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.client.CjesApiClient;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDetailsCJES;

@Component
public class CjesApiClientFallback implements CjesApiClient {

    @Override
    public ResponseEntity<Void> sendJudgmentDetailsCJES(JudgmentDetailsCJES judgmentDetailsCJES) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
