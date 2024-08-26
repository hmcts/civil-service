package uk.gov.hmcts.reform.civil.service.judgments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.client.CjesApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDetailsCJES;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportJudgmentsService {

    private final CjesApiClient cjesApiClient;
    private final CjesMapper cjesMapper;
    private final FeatureToggleService featureToggleService;

    public void sendJudgment(CaseData caseData, Boolean isActiveJudgement) {
        JudgmentDetails judgmentDetails = caseData.getActiveJudgment();

        if (!isActiveJudgement && !caseData.getHistoricJudgment().isEmpty()) {
            judgmentDetails = unwrapElements(caseData.getHistoricJudgment()).get(0);
        }

        JudgmentDetailsCJES requestBody = cjesMapper.toJudgmentDetailsCJES(judgmentDetails, caseData);

        if (!featureToggleService.isCjesServiceAvailable()) {
            log.info("Sending judgement details");
            cjesApiClient.sendJudgmentDetailsCJES(requestBody);
        }
    }
}
