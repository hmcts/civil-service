package uk.gov.hmcts.reform.civil.documentmanagement.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadedDocument {

    private final String fileBaseName;
    private final MultipartFile file;
}
