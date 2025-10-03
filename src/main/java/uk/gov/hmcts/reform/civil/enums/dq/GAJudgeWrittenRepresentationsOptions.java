package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GAJudgeWrittenRepresentationsOptions {
    SEQUENTIAL_REPRESENTATIONS("Sequential representations"),
    CONCURRENT_REPRESENTATIONS("Concurrent representations");

    private final String displayedValue;
}
