import { z } from 'zod';

const mediationUnsuccessful = {
  mediationUnsuccessfulReasonsMultiSelect: z.array(z.string()).min(1),
};

export default {
  mediationUnsuccessful,
};
