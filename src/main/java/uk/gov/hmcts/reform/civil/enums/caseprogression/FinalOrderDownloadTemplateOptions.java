package uk.gov.hmcts.reform.civil.enums.caseprogression;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FinalOrderDownloadTemplateOptions {

    BLANK_TEMPLATE_AFTER_HEARING("Blank template to be used after a hearing"),
    BLANK_TEMPLATE_BEFORE_HEARING("Blank template to be used before a hearing/box work"),
    FIX_DATE_CCMC("Fix a date for CCMC"),
    FIX_DATE_CMC("Fix a date for CMC");

    private final String label;
}
