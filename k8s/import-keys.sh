#!/bin/bash

kubectl create configmap -n trading-journal trading-journal-private-key \
    --from-file=private_key.pem=<FILE_LOCATION>

kubectl create configmap -n trading-journal trading-journal-public-key \
    --from-file=public_key.pem=<FILE_LOCATION>
