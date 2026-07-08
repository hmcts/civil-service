package uk.gov.hmcts.reform.civil.exceptions;

import lombok.Getter;

import java.util.Set;

@Getter
public class InvalidGeneralApplicationTypeException extends RuntimeException {

    public static final String FIELD_PATH = "generalAppType.types";
    private static final String ERROR_MESSAGE = "Invalid general application type";

    private final int invalidValueCount;
    private final Set<String> reasonCategories;

    public InvalidGeneralApplicationTypeException(int invalidValueCount, Set<String> reasonCategories) {
        super(ERROR_MESSAGE);
        this.invalidValueCount = invalidValueCount;
        this.reasonCategories = Set.copyOf(reasonCategories);
    }

}
