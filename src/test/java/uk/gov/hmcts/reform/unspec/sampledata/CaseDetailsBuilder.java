package uk.gov.hmcts.reform.unspec.sampledata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.enums.CaseState;
import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.unspec.enums.CaseState.CREATED;

@SuppressWarnings("unchecked")
public class CaseDetailsBuilder {

    private ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private String state;
    private Map<String, Object> data;

    public static CaseDetailsBuilder builder() {
        return new CaseDetailsBuilder();
    }

    public CaseDetailsBuilder state(CaseState state) {
        this.state = state.name();
        return this;
    }

    public CaseDetailsBuilder data(CaseData caseData) {
        this.data = mapper.convertValue(caseData, Map.class);
        return this;
    }

    public CaseDetailsBuilder atStateExtensionRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateExtensionRequested().build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = CREATED.name();
        return this;
    }

    public CaseDetails build() {
        return CaseDetails.builder()
            .data(data)
            .state(state)
            .build();
    }
}
