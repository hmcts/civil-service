package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentFileType {
    PDF("pdf"),
    DOCX("docx");

    private final String value;
}
