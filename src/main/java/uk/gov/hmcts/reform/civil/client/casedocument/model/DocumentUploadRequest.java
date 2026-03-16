package uk.gov.hmcts.reform.civil.client.casedocument.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@Getter
public class DocumentUploadRequest {

    private String classification;
    private String caseTypeId;
    private String jurisdictionId;
    private List<MultipartFile> files;
}
