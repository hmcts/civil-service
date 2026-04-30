package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;

@Service
@Slf4j
public class FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService extends ElasticSearchService {

    public FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        log.info("Call to FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService query with index {} and timeNow {}", startIndex, timeNow);
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .mustNot(matchQuery("data.fullAdmitNoPaymentSchedulerProcessed", "Yes"))
                            .must(rangeQuery(
                                "data.respondToClaimAdmitPartLRspec.whenWillThisAmountBePaid")
                                      .gte(getLowerBound(timeNow)).lte(getUpperBound(timeNow)))
                            .must(matchQuery("data.respondent1ClaimResponseTypeForSpec",
                        RespondentResponseType.FULL_ADMISSION
                    ))
                            .must(beState(AWAITING_APPLICANT_INTENTION))
                ),
            List.of("reference"),
            startIndex
        );
    }

    private String getUpperBound(String timeNow) {
        LocalDate date = ZonedDateTime.parse(timeNow).withZoneSameInstant(ZoneOffset.UTC).toLocalDate().minusDays(1);
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private String getLowerBound(String timeNow) {
        LocalDate date = DateUtils.addDaysSkippingWeekends(
            ZonedDateTime.parse(timeNow).withZoneSameInstant(ZoneOffset.UTC).toLocalDate().minusDays(8), 0);
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}
