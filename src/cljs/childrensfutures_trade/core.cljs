(ns childrensfutures-trade.core
  (:require
    [cljs-time.extend]
    [cljsjs.material-ui]
    [cljsjs.react-flexbox-grid]
    [cljsjs.web3]
    [childrensfutures-trade.handlers]
    [childrensfutures-trade.subs]
    [childrensfutures-trade.views :as views]
    [print.foo.preloads.devtools]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    ))


(enable-console-print!)

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize])
  (mount-root))
