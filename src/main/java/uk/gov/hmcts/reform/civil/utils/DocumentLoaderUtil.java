package uk.gov.hmcts.reform.civil.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public class DocumentLoaderUtil {

    private DocumentLoaderUtil() {
        //NO-OP
    }

    public static byte[] loadResource(String testPdf) throws IOException {
        ClassPathResource resource = new ClassPathResource(testPdf, DocumentLoaderUtil.class);
        try (InputStream inputStream = resource.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }
}
