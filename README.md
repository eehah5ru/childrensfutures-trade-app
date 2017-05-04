# Myfutures.trade app

## Contracts
Contract addresses are listed in [contracts.md](docs/contracts.md)

To deploy new version of contracts you should make following steps

Start parity:
```shell
  parity --rpcport 8546 --warp --chain ropsten --author 0x77d24Aae19C76C4e5a943776425Eef4D73AaF971
```

Run `truffle migrate  --network ropsten`.

Do not forget to update [contracts.md](docs/contracts.md) and `db.cljs`.
