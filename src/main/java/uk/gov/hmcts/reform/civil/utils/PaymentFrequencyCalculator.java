package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;

public class PaymentFrequencyCalculator {

    private static final int NUMBER_OF_WORKING_WEEKS_IN_A_YEAR = 52;
    private static final int NUMBER_OF_MONTHS_IN_A_YEAR = 12;

    private PaymentFrequencyCalculator () {

    }

    public static double calculatePaymentPerMonth(double amount, PaymentFrequencyLRspec paymentFrequencyLRspec) {
        switch (paymentFrequencyLRspec) {
            case ONCE_FOUR_WEEKS -> {
                return calculateWeeklyIncomeIntoMonthly(4, amount);
            }
            case ONCE_THREE_WEEKS -> {
                return calculateWeeklyIncomeIntoMonthly(3, amount);
            }
            case ONCE_TWO_WEEKS -> {
                return calculateWeeklyIncomeIntoMonthly(2, amount);
            }
            case ONCE_ONE_WEEK -> {
                return calculateWeeklyIncomeIntoMonthly(amount);
            }
            default -> {
                return amount;
            }
        }
    }

    public static double calculateWeeklyIncomeIntoMonthly(int weeks, double amount) {
        return calculateWeeklyIncomeIntoMonthly(amount / weeks);
    }

    public static double calculateWeeklyIncomeIntoMonthly(double amount) {
        return Math.round(amount * NUMBER_OF_WORKING_WEEKS_IN_A_YEAR / NUMBER_OF_MONTHS_IN_A_YEAR);
    }

}
