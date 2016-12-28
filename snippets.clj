;;; start server
(do
  (childrensfutures-trade.core/-main)
  (figwheel-sidecar.repl-api/start-figwheel! (figwheel-sidecar.config/fetch-config))
  (figwheel-sidecar.repl-api/cljs-repl))
