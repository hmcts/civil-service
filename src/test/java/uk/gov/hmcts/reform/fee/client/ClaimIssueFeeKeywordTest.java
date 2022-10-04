package uk.gov.hmcts.reform.fee.client;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.*;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;
import uk.gov.hmcts.reform.fees.client.ClaimIssueFeeKeyword;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_TWO;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.*;
import static uk.gov.hmcts.reform.civil.sampledata.PartyBuilder.DATE_OF_BIRTH;

class ClaimIssueFeeKeywordTest {

    @Nested
    class GetKeywordIssueEvent {
        @Test
        void shouldReturnUnspecifiedClaim_isSpecifiedAndAmountIsZero() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(10.00), false) ;
            assertEquals(keyword, "UnspecifiedClaim");
        }


    }
}
