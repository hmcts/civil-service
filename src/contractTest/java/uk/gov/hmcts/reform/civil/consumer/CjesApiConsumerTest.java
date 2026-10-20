package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.civil.client.CjesApiClient;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentAddress;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDefendantDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDetailsCJES;

import java.time.LocalDate;
import java.time.LocalDateTime;

@PactTestFor(providerName = "rtl")
@MockServerConfig(hostInterface = "localhost", port = "6685")
@TestPropertySource(properties = "rtl.api.url=http://localhost:6685")
public class CjesApiConsumerTest extends BaseContractTest {

    private static final String JUDGMENT_ID = "J-0001";

    @Autowired
    private CjesApiClient cjesApiClient;

    @Pact(consumer = "civil_service")
    public RequestResponsePact sendJudgment(PactDslWithProvider builder) throws Exception {
        return builder
            .uponReceiving("a CJES judgment registration request")
            .path("/judgment")
            .method(HttpMethod.POST.toString())
            .body(createJsonObject(buildJudgmentDetails()))
            .willRespondWith()
            .status(HttpStatus.SC_CREATED)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "sendJudgment")
    public void verifySendJudgment() {
        cjesApiClient.sendJudgmentDetailsCJES(buildJudgmentDetails());
    }

    private JudgmentDetailsCJES buildJudgmentDetails() {
        return new JudgmentDetailsCJES(
            "AAA7",
            JUDGMENT_ID,
            LocalDateTime.of(2026, 8, 14, 10, 15, 30),
            "20262",
            "1712345678901234",
            "000MC001",
            100.00,
            LocalDate.of(2026, 8, 14),
            "judgmentRegistered",
            null,
            buildDefendant(),
            null
        );
    }

    private JudgmentDefendantDetails buildDefendant() {
        return new JudgmentDefendantDetails(
            "Jane Smith",
            LocalDate.of(1980, 1, 1),
            new JudgmentAddress("1 High Street", null, null, null, "London", "SW1A 1AA")
        );
    }
}
