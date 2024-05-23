package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DocumentLoaderUtilTest {

    @Test
    void readBytesFromGiveFileFromPath() throws IOException {
        String fixture = "/fixture/go1protected.pdf";
        assertNotNull(DocumentLoaderUtil.loadResource(fixture));
    }
}
