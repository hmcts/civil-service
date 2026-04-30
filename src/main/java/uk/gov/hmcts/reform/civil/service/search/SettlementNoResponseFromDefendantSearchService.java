package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Service
@Slf4j
public class SettlementNoResponseFromDefendantSearchService extends ElasticSearchService {

    private static final int BUSINESS_DAYS_FROM_NOW = 0;
    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0, 0);

    public SettlementNoResponseFromDefendantSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        log.info("Call to SettlementNoResponseFromDefendantSearchService query with index {} and timeNow {}", startIndex, timeNow);
        String targetDateString = calculateTargetDate(timeNow);
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(matchQuery(
                                "data.respondent1RespondToSettlementAgreementDeadline",
                                targetDateString
                            ))
                            .must(beState(CaseState.AWAITING_APPLICANT_INTENTION))
                ),
            List.of("reference"),
            startIndex
        );
    }

    private String calculateTargetDate(String timeNow) {
        LocalDate baseDate = ZonedDateTime.parse(timeNow)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDate();
        return DateUtils.addDaysSkippingWeekends(baseDate.minusDays(1), BUSINESS_DAYS_FROM_NOW)
            .atTime(END_OF_BUSINESS_DAY)
            .format(DateTimeFormatter.ISO_DATE);
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}
