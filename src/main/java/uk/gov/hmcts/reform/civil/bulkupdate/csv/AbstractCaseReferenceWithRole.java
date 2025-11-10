package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public abstract class AbstractCaseReferenceWithRole<T> extends CaseReference implements ExcelMappable {

    protected T dataObject;
    protected boolean isApplicant1;
    protected boolean isApplicant2;
    protected boolean isRespondent1;
    protected boolean isRespondent2;

    protected final ObjectMapper mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    protected AbstractCaseReferenceWithRole() {
        // required by Lombok @SuperBuilder
    }

    protected enum Role {
        APPLICANT1,
        APPLICANT2,
        RESPONDENT1,
        RESPONDENT2
    }

    @Override
    public void fromExcelRow(Map<String, Object> rowValues) throws Exception {
        if (rowValues.containsKey("caseReference")) {
            Object value = rowValues.get("caseReference");
            setCaseReference(value != null ? value.toString() : null);
        }

        for (var entry : rowValues.entrySet()) {
            String key = entry.getKey();

            Role role = switch (key) {
                case "applicant1", "applicant1LitigationFriend" -> Role.APPLICANT1;
                case "applicant2", "applicant2LitigationFriend" -> Role.APPLICANT2;
                case "respondent1", "respondent1LitigationFriend" -> Role.RESPONDENT1;
                case "respondent2", "respondent2LitigationFriend" -> Role.RESPONDENT2;
                default -> null;
            };

            if (role != null) {
                populateObject(entry.getValue(), role);
                break;
            }
        }
    }

    private void populateObject(Object value, Role role) throws Exception {
        if (getObjectType().isInstance(value)) {
            this.dataObject = getObjectType().cast(value);
        } else {
            String json = (value instanceof String strValue)
                ? strValue // already JSON
                : mapper.writeValueAsString(value);
            this.dataObject = mapper.readValue(json, getObjectType());
        }

        this.isApplicant1 = false;
        this.isApplicant2 = false;
        this.isRespondent1 = false;
        this.isRespondent2 = false;

        switch (role) {
            case APPLICANT1 -> this.isApplicant1 = true;
            case APPLICANT2 -> this.isApplicant2 = true;
            case RESPONDENT1 -> this.isRespondent1 = true;
            case RESPONDENT2 -> this.isRespondent2 = true;
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    /** Each subclass defines which object type (Party or LitigationFriend) should be mapped. */
    protected abstract Class<T> getObjectType();
}
