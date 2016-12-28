#!/usr/bin/env bash
CONTRACTS_PATH="$(pwd)/resources/public/contracts/"
SRC_PATH="$CONTRACTS_PATH/src"
BUILD_PATH="$CONTRACTS_PATH/build"
cd $SRC_PATH
solc --optimize --bin --abi --combined-json bin,abi GoalsStockExchange.sol -o $BUILD_PATH > $BUILD_PATH/GoalsStockExchange.json
