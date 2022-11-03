package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class EvidenceUploadNotificationSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new EvidenceUploadNotificationSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
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
                          .should(boolQuery().must(matchQuery("state", "CASE_PROGRESSION")))
                          .should(boolQuery().must(matchQuery("state", "HEARING_READINESS")))
                          .should(boolQuery().must(matchQuery("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING")))
                          .minimumShouldMatch(1)
                );

        return new Query(query, List.of("reference"), fromValue);
    }
}
