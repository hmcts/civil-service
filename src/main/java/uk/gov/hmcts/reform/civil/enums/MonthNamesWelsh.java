package uk.gov.hmcts.reform.civil.enums;

public enum MonthNamesWelsh {

    JANUARY("Ionawr", 1),
    FEBRUARY("Chwefror", 2),
    MARCH("Mawrth", 3),
    APRIL("Ebrill", 4),
    MAY("Mai", 5),
    JUNE("Mehefin", 6),
    JULY("Gorffennaf", 7),
    AUGUST("Awst", 8),
    SEPTEMBER("Medi", 9),
    OCTOBER("Hydref", 10),
    NOVEMBER("Tachwedd", 11),
    DECEMBER("Rhagfyr", 12);

    private String welshName;
    private int value;

    MonthNamesWelsh(final String text, final int value) {
        this.welshName = text;
        this.value = value;
    }

    public String getWelshName() {
        return this.welshName;
    }

    public int getValue() {
        return this.value;
    }

    public static String getWelshNameByValue(int value) {
        for (MonthNamesWelsh monthName : values()) {
            if (monthName.getValue() == value) {
                return monthName.getWelshName();
            }
        }
        throw new IllegalArgumentException("Invalid value for month name: " + value);
    }
}
