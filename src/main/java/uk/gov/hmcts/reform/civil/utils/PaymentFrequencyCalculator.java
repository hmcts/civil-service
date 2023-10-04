package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;

public class PaymentFrequencyCalculator {

    private PaymentFrequencyCalculator () {

    }

    public static double calculatePaymentPerMonth(int amount, PaymentFrequencyLRspec paymentFrequencyLRspec) {
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
        return amount * 52 / 12;
    }

}
