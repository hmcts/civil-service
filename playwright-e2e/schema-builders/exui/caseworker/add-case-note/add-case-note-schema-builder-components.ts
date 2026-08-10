import { z } from 'zod';

const caseNotes = {
  caseNotes: z.array(z.looseObject({})).min(1),
};

const addCaseNoteSchemaBuilderComponents = {
  caseNotes,
};

export default addCaseNoteSchemaBuilderComponents;
