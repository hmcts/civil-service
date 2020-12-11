package uk.gov.hmcts.reform.unspec.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
    WELSH("Welsh"),
    ENGLISH("English"),
    BOTH("Welsh and English");

    private final String displayedValue;
}
