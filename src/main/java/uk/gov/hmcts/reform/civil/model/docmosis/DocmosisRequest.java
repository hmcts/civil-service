package uk.gov.hmcts.reform.civil.model.docmosis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisRequest {

    private String accessKey;
    private String templateName;
    private String outputFormat;
    private String outputName;
    private Map<String, Object> data;
}
