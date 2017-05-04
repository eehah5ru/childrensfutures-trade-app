#!/bin/bash

echo "deploying to myfutures.trade" &&
    # lein clean &&
    lein with-profile production filegen-ng &&
    LEIN_SNAPSHOTS_IN_RELEASE=true lein with-profile production uberjar &&
    scp ./target/childrensfutures-trade.jar do.myfutures.trade:/var/myfutures-trade &&
    ssh do.myfutures.trade "sudo service myfutures-trade restart; sleep 10; sudo service myfutures-trade status"
