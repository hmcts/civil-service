package uk.gov.hmcts.reform.civil.scheduler.garesponsedeadline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;

@Service
@RequiredArgsConstructor
@Slf4j
public class GAResponseDeadlineSearchService {

    private final CaseStateSearchService searchService;
    private final GAResponseDeadlineFilter filter;

    public TaskResult<CaseDetails> getElasticSearchResult() {
        List<CaseDetails> casesToProcess = new ArrayList<>();

        // 1. Applications awaiting respondent response past deadline
        casesToProcess.addAll(
            searchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE)
                .stream()
                .filter(filter::isDeadlineExpired)
                .toList()
        );

        // 2. Urgent applications in judicial review pending notification check
        casesToProcess.addAll(
            searchService.getUncheckedResponseDeadlineApplications(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .stream()
                .filter(filter::isDeadlineExpired)
                .toList()
        );

        return new ListTaskResult<>(casesToProcess);
    }
}
