export type TestSpeed = {
  key: 'fast' | 'medium' | 'slow';
  slowMo: number;
};

type TestSpeeds = {
  SLOW: TestSpeed;
  MEDIUM: TestSpeed;
  FAST: TestSpeed;
};

export default TestSpeeds;
