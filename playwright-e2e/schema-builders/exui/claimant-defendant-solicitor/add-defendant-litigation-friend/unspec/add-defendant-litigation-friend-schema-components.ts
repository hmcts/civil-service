import { z } from 'zod';

const nonEmptyString = z.string().min(1);

const respondent1LitigationFriend = {
  respondent1LitigationFriend: z.looseObject({
    firstName: nonEmptyString,
    lastName: nonEmptyString,
    emailAddress: nonEmptyString,
    phoneNumber: nonEmptyString,
    hasSameAddressAsLitigant: nonEmptyString,
    primaryAddress: z.looseObject({}),
    certificateOfSuitability: z.array(z.looseObject({})).min(1),
  }),
};

const addDefendantLitigationFriendSchemaComponents = {
  respondent1LitigationFriend,
};

export default addDefendantLitigationFriendSchemaComponents;
