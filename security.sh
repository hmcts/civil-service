#!/bin/bash
#echo "${SECURITYCONTEXT}" > /zap/security.context
export LC_ALL=C.UTF-8
export LANG=C.UTF-8

ZAP_PORT=1001
ZAP_HOST=0.0.0.0
DEBUG=false

# ------------------------------------------------------------------

if [ "$DEBUG" == "true" ]; then
    ZAP_PORT=8080
    TEST_URL=https://idam-web-public.aat.platform.hmcts.net

    # start ZAP locally
    echo "Starting a local instance of ZAP..."
    #docker pull owasp/zap2docker-weekly:latest
    docker run -d -u zap -p $ZAP_PORT:$ZAP_PORT owasp/zap2docker-weekly zap-x.sh \
        -d \
        -host $ZAP_HOST \
        -port $ZAP_PORT \
        -config api.disablekey=true \
        -config scanner.attackOnStart=true \
        -config view.mode=attack \
        -config connection.dnsTtlSuccessfulQueries=-1 \
        -config api.addrs.addr.name=".*" \
        -config api.addrs.addr.regex=true
else
    zap-x.sh -d -host $ZAP_HOST -port $ZAP_PORT -config api.disablekey=true -config scanner.attackOnStart=true -config view.mode=attack -config rules.cookie.ignorelist=_ga,_gid,_gat,dtCookie,dtLatC,dtPC,dtSa,rxVisitor,rxvt -config connection.dnsTtlSuccessfulQueries=-1 -config api.addrs.addr.name=.* -config api.addrs.addr.regex=true /dev/null 2>&1 &
fi

# Wait for ZAP to start
printf "Waiting for ZAP to start"
i=0

while ! (curl -s http://${ZAP_HOST}:${ZAP_PORT}) >/dev/null; do
    i=$(((i + 1) % 5))
    if [ $i -eq 0 ]; then
        printf "."
    fi
    sleep .2
done
echo
echo "ZAP has successfully started"

zap-cli --zap-url http://$ZAP_HOST -p $ZAP_PORT status -t 120
zap-cli --zap-url http://$ZAP_HOST -p $ZAP_PORT open-url "${TEST_URL}"
xargs -I % echo "Excluding regexp: %" <zap-exclusions
xargs -I % zap-cli --zap-url http://$ZAP_HOST -p $ZAP_PORT exclude % <zap-exclusions
zap-cli --zap-url http://$ZAP_HOST -p $ZAP_PORT spider ${TEST_URL}
zap-cli --zap-url http://$ZAP_HOST -p $ZAP_PORT active-scan --scanners all --recursive "${TEST_URL}"
zap-cli --zap-url http://$ZAP_HOST -p $ZAP_PORT report -o activescan.html -f html
echo 'Changing owner from $(id -u):$(id -g) to $(id -u):$(id -u)'
chown -R $(id -u):$(id -u) activescan.html
curl --fail http://${ZAP_HOST}:${ZAP_PORT}/OTHER/core/other/jsonreport/?formMethod=GET --output report.json
cp *.html functional-output/
zap-cli --zap-url http://$ZAP_HOST -p $ZAP_PORT alerts -l High --exit-code False

# INFO: in order to add more exclusions for low-level issues, please do the following:
# - Extract the JSON output of the security scan from the build (an array of objects, each beginning with "task":"OWASP Zaproxy")
# - Transform it with jq using the following query: map({(.fingerprint):"ignore"})|add
# - Add the entries you are interested in to audit.json
