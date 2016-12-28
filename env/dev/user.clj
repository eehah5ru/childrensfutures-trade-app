 (ns user
   (:require [figwheel-sidecar.repl-api]
             [childrensfutures-trade.core]
             [ring.middleware.reload :refer [wrap-reload]]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def http-handler
  (wrap-reload #'childrensfutures-trade.core/http-handler))
