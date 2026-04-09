package uk.gov.hmcts.reform.civil.ras.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Attributes {

    private String substantive;
    private String caseId;
    private String jurisdiction;
    private String caseType;
    private String primaryLocation;
    private String region;
    private String contractType;
}
