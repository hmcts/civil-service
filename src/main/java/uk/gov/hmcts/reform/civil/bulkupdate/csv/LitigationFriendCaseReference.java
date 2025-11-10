package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({"caseReference"})
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1700")
public class LitigationFriendCaseReference extends AbstractCaseReferenceWithRole<LitigationFriend> {

    @JsonProperty
    private LitigationFriend litigationFriend;

    @Override
    protected Class<LitigationFriend> getObjectType() {
        return LitigationFriend.class;
    }

    @Override
    public void fromExcelRow(Map<String, Object> rowValues) throws Exception {
        super.fromExcelRow(rowValues); // sets dataObject
        this.litigationFriend = this.dataObject;  // sync CSV field
    }
}
