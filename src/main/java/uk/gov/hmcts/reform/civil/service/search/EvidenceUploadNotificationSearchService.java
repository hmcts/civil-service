package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;

@Service
public class EvidenceUploadNotificationSearchService extends ElasticSearchService {

    public EvidenceUploadNotificationSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {
        return new Query(
            boolQuery()
                .must(boolQuery()
                            .should(rangeQuery("data.documentUploadDisclosure1.value.createdDatetime").lt("now").gt(
                                "now-1d"))
                            .should(rangeQuery("data.documentUploadDisclosure2.value.createdDatetime").lt("now").gt(
                                "now-1d"))
                            .should(rangeQuery("data.documentUploadWitness1.value.createdDatetime").lt("now").gt(
                                "now-1d"))
                            .should(rangeQuery("data.documentUploadWitness2.value.createdDatetime").lt("now").gt(
                                "now-1d"))
                            .should(rangeQuery("data.documentUploadWitness3.value.createdDatetime").lt("now").gt(
                                "now-1d"))
                            .should(rangeQuery("data.documentUploadWitness4.value.createdDatetime").lt("now").gt(
                                "now-1d"))
                            .should(rangeQuery("data.documentUploadExpert1.value.createdDatetime").lt("now").gt(
                                "now-1d"))
                            .should(rangeQuery("data.documentUploadExpert2.value.createdDatetime").lt("now").gt(
                                "now-1d"))
                            .should(rangeQuery("data.documentUploadExpert3.value.createdDatetime").lt("now").gt(
                                "now-1d"))
                            .should(rangeQuery("data.documentUploadExpert4.value.createdDatetime").lt("now").gt(
                                "now-1d"))
                            .should(rangeQuery("data.documentUploadTrial1.value.createdDatetime").lt("now").gt(
                        "now-1d"))
                            .should(rangeQuery("data.documentUploadTrial2.value.createdDatetime").lt("now").gt(
                                "now-1d"))
                            .should(rangeQuery("data.documentUploadTrial3.value.createdDatetime").lt("now").gt(
                                "now-1d"))
                            .should(rangeQuery("data.documentUploadTrial4.value.createdDatetime").lt("now").gt(
                        "now-1d"))
                            .minimumShouldMatch(1)
                )
                .must(boolQuery()
                          .should(beState(CASE_PROGRESSION))
                          .should(beState(HEARING_READINESS))
                          .should(beState(PREPARE_FOR_HEARING_CONDUCT_HEARING))
                          .minimumShouldMatch(1)
                ),
            List.of("reference"),
            startIndex
        );

    }

    public BoolQueryBuilder beState(CaseState state) {
        return boolQuery()
            .must(matchQuery("state", state.toString()));
    }
}
