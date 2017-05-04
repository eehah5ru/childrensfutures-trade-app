#!/bin/bash

echo "deploying to dev.myfutures.trade" &&
    # lein clean &&
    lein with-profile staging filegen-ng &&
    LEIN_SNAPSHOTS_IN_RELEASE=true lein with-profile staging uberjar &&
    scp ./target/childrensfutures-trade.jar do.myfutures.trade:/var/dev-myfutures-trade &&
    ssh do.myfutures.trade "sudo service dev-myfutures-trade restart; sleep 10; sudo service dev-myfutures-trade status"
