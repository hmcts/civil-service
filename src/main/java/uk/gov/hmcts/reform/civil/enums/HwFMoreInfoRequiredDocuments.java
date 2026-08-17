package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "HwfInformationProofList", generate = true)
@Getter
@RequiredArgsConstructor
public enum HwFMoreInfoRequiredDocuments {
    @CCD(label = "Any Other Income")
    ANY_OTHER_INCOME("Any other income", "", "Unrhyw incwm arall", ""),
    @CCD(label = "Bank Statements")
    BANK_STATEMENTS(
        "Bank statements",
        " Last month’s statements for all of the applicant’s current accounts, and if part of a "
            + "couple, their partner’s bank statements. Bank statement should be for full month prior to "
            + "the date the fee is to be paid, for example, if the fee is to be paid on 26 July 2021 the"
            + "bank statement should be for June 2021.",
        "Cyfriflenni banc",
        "Cyfriflenni banc y mis diwethaf ar gyfer holl gyfrifon cyfredol y ceisydd, ac os yw’n rhan"
            + " o gwpl, cyfriflenni banc eu partner. Dylai’r gyfriflen banc fod ar gyfer y mis cyn y dyddiad y mae’r "
            + "ffi i’w thalu, er enghraifft, os yw’r ffi i’w thalu ar 26 Gorffennaf 2021, dylai’r gyfriflen banc fod ar"
            + " gyfer Mehefin 2021."
    ),
    @CCD(label = "Benefits and Tax Credits")
    BENEFITS_AND_TAX_CREDITS(
        "Benefits and tax credits",
        "A letter or document dated in the last 3 months, of contribution-based Job "
            + "Seekers Allowance (JSA) contribution-based Employment and Support Allowance (JSA), "
            + "Universal Credit, Child Benefit, Working Tax Credit or Child Tax Credit",
        "Budd-daliadau a chredydau treth",
        "Llythyr neu ddogfen wedi’i dyddio yn y 3 mis diwethaf, o Lwfans Ceisio Gwaith yn Seiliedig ar"
            + " Gyfraniadau (JSA), Lwfans Cyflogaeth a Chymorth yn seiliedig ar Gyfraniadau (ESA), Credyd Cynhwysol, "
            + "Budd-dal Plant, Credyd Treth Gwaith neu Gredyd Treth Plant"
    ),
    @CCD(label = "Child Maintenance")
    CHILD_MAINTENANCE(
        "Child maintenance",
        " Evidence of being in receipt of Child Maintenance, such as a Child Support "
            + "Agency assessment, sealed court order or letter of agreement showing how often and much"
            + " you’re paid",
        "Cynhaliaeth plant",
        "Tystiolaeth o dderbyn Cynhaliaeth Plant, megis asesiad gan yr Asiantaeth Cynnal Plant,"
            + " gorchymyn llys dan sêl; neu llythyr o gytundeb yn dangos pa mor aml rydych yn cael eich a faint rydych "
            + "yn cael eich talu"
    ),
    @CCD(label = "Income from Selling Goods")
    INCOME_FROM_SELLING_GOODS("Income from Selling Goods", "", "Incwm o Werthu Nwyddau",
                              ""
    ),
    @CCD(label = "Pensions")
    PENSIONS("Pensions", "Evidence from Pensions", "Pensiynau",
             "Tystiolaeth o Bensiynau"
    ),
    @CCD(label = "Prisoner's Income")
    PRISONERS_INCOME("Prisoners income", "", "Incwm carcharor", ""),
    @CCD(label = "Rental Income")
    RENTAL_INCOME("Rental income", "Evidence from Rental Income", "Incwm o rentu",
                  "Tystiolaeth gan Rentu"
    ),
    @CCD(label = "Wage Slips")
    WAGE_SLIPS(
        "Wage slips",
        " If you’re employed, a payslip dated in the last 6 weeks, or, if you’re self-employed, your"
            + " most recent self-assessment tax return and SA302 tax calculation which you can get from "
            + "www.gov.uk/sa302-tax-calculation",
        "Slipiau cyflog",
        " Os ydych yn gyflogedig, slip cyflog wedi’i ddyddio yn y 6 wythnos ddiwethaf, neu os ydych"
            + " yn hunangyflogedig, eich ffurflen dreth hunanasesiad diweddaraf a chyfrifiad treth SA302 y gallwch ei "
            + "gael yn www.gov.uk/sa302-tax-calculation"
    );

    private final String name;
    private final String description;
    private final String nameBilingual;
    private final String descriptionBilingual;
}
