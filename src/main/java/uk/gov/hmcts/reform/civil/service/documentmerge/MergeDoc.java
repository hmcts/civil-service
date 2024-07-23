package uk.gov.hmcts.reform.civil.service.documentmerge;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class MergeDoc {

    private byte[] file;
    private String sectionHeader;
}
