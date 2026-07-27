export type AxeResults = {
  [key: string]: PageResult | undefined;
};

export type PageResult = {
  testName: string;
  pass: boolean;
  screenshotInfo?: {
    fileName: string;
    filePath: string;
  };
  violationsInfo?: {
    length: number;
    fileName: string;
    filePath: string;
  };
};
