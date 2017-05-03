#!/bin/bash

echo "deploying to dev.myfutures.trade" &&
    lein uberjar &&
    scp ./target/childrensfutures-trade.jar do.myfutures.trade:/var/dev-myfutures-trade &&
    ssh do.myfutures.trade "sudo service dev-myfutures-trade restart; sleep 10; sudo service dev-myfutures-trade status"
