#!/usr/bin/env bash
# The ZAP scan the nightly pipeline runs (the 'Security scan' stage in Jenkinsfile_nightly), in place of the pipeline library's
# default backend script (cnp-jenkins-library resources/uk/gov/hmcts/pipeline/security/backend/security.sh), which this
# follows with three changes:
#  - civil-service publishes its OpenAPI spec with springdoc at /v3/api-docs, not /v2/api-docs
#  - the spec is requested until it is ready before ZAP starts (see below)
#  - --hook=zap_hooks.py applies the rule suppressions documented in security.md
# Findings are then checked against audit.json by the pipeline library.
echo ${TEST_URL}
if [[ -z ${SecurityRules} ]]; then
  echo "SecurityRules variable not set. Please set it in Jenkinsfile_nightly."
  exit 1
fi
# springdoc builds the OpenAPI spec on its first request, which can take longer than the gateway's 30 second timeout
# (504), then serves it at once. Ask for it until it is ready, so ZAP gets the spec rather than the timeout.
for attempt in 1 2 3 4 5; do
  status=$(curl -s -o /dev/null --max-time 120 -w '%{http_code}' ${TEST_URL}/v3/api-docs)
  echo "OpenAPI spec attempt ${attempt}: ${status}"
  if [[ ${status} == 200 ]]; then
    break
  fi
  sleep 10
done
ZAP_URL_EXCLUSIONS="-config globalexcludeurl.url_list.url\(0\).regex='^https?:\/\/.*\/(?:.*login.*)+$' ${ZAP_URL_EXCLUSIONS}"
echo ${ZAP_URL_EXCLUSIONS}
zap-api-scan.py -t ${TEST_URL}/v3/api-docs -f openapi -S -d -u ${SecurityRules} -P 1001 --hook=zap_hooks.py -z "-config rules.cookie.ignorelist=_ga,_gid,_gat,dtCookie,dtLatC,dtPC,dtSa,rxVisitor,rxvt" -l FAIL -r /zap/wrk/api-report.html
curl --fail http://0.0.0.0:1001/OTHER/core/other/jsonreport/?formMethod=GET --output report.json
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
mkdir -p functional-output
chmod a+wx functional-output
cp /zap/wrk/api-report.html functional-output/
cp *.* functional-output/
