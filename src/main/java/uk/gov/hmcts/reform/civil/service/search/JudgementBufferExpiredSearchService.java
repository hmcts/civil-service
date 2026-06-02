package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.calculator.SearchDateTimeCalculator;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static uk.gov.hmcts.reform.civil.helpers.LocalDateTimeHelper.LOCAL_ZONE;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
@Slf4j
public class JudgementBufferExpiredSearchService extends ElasticSearchService {

    private final SearchDateTimeCalculator dateTimeCalculator;

    public JudgementBufferExpiredSearchService(CoreCaseDataService coreCaseDataService,
                                               SearchDateTimeCalculator dateTimeCalculator) {
        super(coreCaseDataService);
        this.dateTimeCalculator = dateTimeCalculator;
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        log.info("Call to JudgementBufferExpiredSearchService query with index {} and timeNow {}", startIndex, timeNow);
        ZonedDateTime now = ZonedDateTime.parse(timeNow).withZoneSameInstant(LOCAL_ZONE);
        ZonedDateTime timeMinus48WorkingHours = dateTimeCalculator.minusWorkingHours(now, 48);
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.joDJCreatedDate").lte(formatToIsoLocal(timeMinus48WorkingHours)))
                            .must(beState(CaseState.JUDGMENT_REQUESTED))
                            .must(haveNoOngoingBusinessProcess())
                ),
            List.of("reference"),
            startIndex,
            true
        );
    }

    public BoolQueryBuilder beState(CaseState state) {
        return boolQuery()
            .must(matchQuery("state", state.toString()));
    }

    public BoolQueryBuilder haveNoOngoingBusinessProcess() {
        return boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery().mustNot(existsQuery("data.businessProcess")))
            .should(boolQuery().mustNot(existsQuery("data.businessProcess.status")))
            .should(boolQuery().must(matchQuery("data.businessProcess.status", "FINISHED")));
    }

    private String formatToIsoLocal(ZonedDateTime timeMinus48WorkingHours) {
        return timeMinus48WorkingHours.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
