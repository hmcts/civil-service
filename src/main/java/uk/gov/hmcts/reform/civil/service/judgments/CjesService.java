package uk.gov.hmcts.reform.civil.service.judgments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.client.CjesApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDetailsCJES;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Slf4j
@Service
@RequiredArgsConstructor

public class CjesService {

    private final CjesApiClient cjesApiClient;
    private final CjesMapper cjesMapper;
    private final FeatureToggleService featureToggleService;

    public void sendJudgment(CaseData caseData, Boolean isActiveJudgement) {
        try {
            JudgmentDetailsCJES requestBody = cjesMapper.toJudgmentDetailsCJES(caseData, isActiveJudgement);

            log.info("Sending judgement details...");

            if (featureToggleService.isCjesServiceAvailable()) {
                // Feign client is not ready yet. Will be added later on in JO feature.
                cjesApiClient.sendJudgmentDetailsCJES(requestBody);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to send judgment to RTL");
        }
    }
}
