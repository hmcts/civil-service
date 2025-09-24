import TestSpeeds from '../models/test-speeds.ts';

const testSpeeds: TestSpeeds = {
  SLOW: {
    key: 'slow',
    slowMo: 600,
  },
  MEDIUM: {
    key: 'medium',
    slowMo: 300,
  },
  FAST: {
    key: 'fast',
    slowMo: 0,
  },
};

export default testSpeeds;
