package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ClaimTimelineList", generate = true)
@Getter
@RequiredArgsConstructor
public enum TimelineUploadTypeSpec {
    @CCD(label = "Upload claim timeline template")
    UPLOAD("Upload"),
    @CCD(label = "Add manually")
    MANUAL("Manual");

    private final String displayedValue;
}
