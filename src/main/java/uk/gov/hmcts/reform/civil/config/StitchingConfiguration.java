package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class StitchingConfiguration {

    private final String stitchingUrl;

    public StitchingConfiguration(@Value("${stitching.api.url}") String stitchingUrl) {
        this.stitchingUrl = stitchingUrl;
    }
}
