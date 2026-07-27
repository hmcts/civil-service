import { z } from 'zod';

const defendantDetails = () => ({
  defendantDetails: z.looseObject({
    list_items: z.array(
      z.looseObject({
        code: z.string(),
        label: z.string(),
      }),
    ),
    value: z.looseObject({
      code: z.string(),
      label: z.string(),
    }),
  }),
});

const hearingType = () => ({
  hearingSelection: z.string(),
  detailsOfDirection: z.string(),
});

const hearingSupportRequirementsFieldDJ = () => ({
  hearingSupportRequirementsDJ: z.looseObject({
    hearingType: z.string(),
    hearingTemporaryLocation: z.looseObject({
      list_items: z.array(
        z.looseObject({
          code: z.string(),
          label: z.string(),
        }),
      ),
      value: z.looseObject({
        code: z.string(),
        label: z.string(),
      }),
    }),
    hearingUnavailableDates: z.string(),
    hearingDates: z.array(
      z.looseObject({
        value: z.looseObject({
          hearingUnavailableFrom: z.string(),
          hearingUnavailableUntil: z.string(),
        }),
      }),
    ),
    hearingSupportQuestion: z.string(),
    hearingSupportAdditional: z.string(),
    hearingPreferredEmail: z.string(),
    hearingPreferredTelephoneNumber1: z.string(),
  }),
});

const defaultJudgementSchemaComponents = {
  defendantDetails,
  hearingType,
  hearingSupportRequirementsFieldDJ,
};

export default defaultJudgementSchemaComponents;
