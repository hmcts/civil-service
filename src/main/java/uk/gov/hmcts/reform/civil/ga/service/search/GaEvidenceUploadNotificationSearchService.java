package uk.gov.hmcts.reform.civil.ga.service.search;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
public class GaEvidenceUploadNotificationSearchService extends GaElasticSearchService {

    public GaEvidenceUploadNotificationSearchService(GaCoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Set<CaseDetails> getApplications() {

        SearchResult searchResult = coreCaseDataService
                .searchGeneralApplication(query(START_INDEX));

        int pages = calculatePages(searchResult);
        List<CaseDetails> caseDetails = new ArrayList<>(searchResult.getCases());

        for (int i = 1; i < pages; i++) {
            caseDetails.addAll(coreCaseDataService
                                   .searchGeneralApplication(query(i * ES_DEFAULT_SEARCH_LIMIT)).getCases());
        }

        return new HashSet<>(caseDetails);
    }

    public Query query(int startIndex) {
        return new Query(
                boolQuery()
                    .minimumShouldMatch(1)
                    .should(rangeQuery("data.caseDocumentUploadDate").lt("now").gt(
                        "now-1d"))
                    .should(rangeQuery("data.caseDocumentUploadDateRes").lt("now").gt(
                        "now-1d")),
                List.of("reference"),
                startIndex
        );
    }

    @Override
    Query query(final int startIndex, final CaseState caseState) {
        return null;
    }

    @Override
    Query queryForOrderMade(final int startIndex, final CaseState caseState, final GeneralApplicationTypes gaType) {
        return null;
    }

    @Override
    Query queryForBusinessProcessStatus(final int startIndex, final BusinessProcessStatus processStatus) {
        return null;
    }
}
