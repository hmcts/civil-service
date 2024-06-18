package uk.gov.hmcts.reform.civil.model.documents;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadedDocument {

    private final String fileBaseName;
    private final MultipartFile file;
}
