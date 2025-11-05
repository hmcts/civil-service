package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder(value = {"caseReference"})
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1700")
public class LitigationFriendCaseReference extends CaseReference implements ExcelMappable {

    @JsonProperty
    private LitigationFriend litigationFriend;
    private boolean isApplicant1;
    private boolean isApplicant2;
    private boolean isRespondent1;
    private boolean isRespondent2;

    @Override
    public void fromExcelRow(Map<String, Object> rowValues) throws Exception {
        ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (rowValues.containsKey("caseReference")) {
            Object value = rowValues.get("caseReference");
            setCaseReference(mapper.writeValueAsString(value));
        }

        // Check for Applicant1 Litigation Friend
        if (rowValues.containsKey("applicant1LitigationFriend")) {
            Object value = rowValues.get("applicant1LitigationFriend");
            String json = mapper.writeValueAsString(value);

            this.litigationFriend = mapper.readValue(json, LitigationFriend.class);
            this.isApplicant1 = true;
            this.isApplicant2 = false;
            this.isRespondent1 = false;
            this.isRespondent2 = false;
        }

        // Check for Applicant2 Litigation Friend
        if (rowValues.containsKey("applicant2LitigationFriend")) {
            Object value = rowValues.get("applicant2LitigationFriend");
            String json = mapper.writeValueAsString(value);

            this.litigationFriend = mapper.readValue(json, LitigationFriend.class);
            this.isApplicant2 = true;
            this.isApplicant1 = false;
            this.isRespondent1 = false;
            this.isRespondent2 = false;
        }

        if (rowValues.containsKey("respondent1LitigationFriend")) {
            Object value = rowValues.get("respondent1LitigationFriend");
            String json = mapper.writeValueAsString(value);

            this.litigationFriend = mapper.readValue(json, LitigationFriend.class);
            this.isRespondent1 = true;

            this.isApplicant1 = false;
            this.isApplicant2 = false;
            this.isRespondent2 = false;
        }

        if (rowValues.containsKey("respondent2LitigationFriend")) {
            Object value = rowValues.get("respondent2LitigationFriend");
            String json = mapper.writeValueAsString(value);

            this.litigationFriend = mapper.readValue(json, LitigationFriend.class);
            this.isRespondent2 = true;

            this.isApplicant1 = false;
            this.isApplicant2 = false;
            this.isRespondent1 = false;
        }
    }
}
