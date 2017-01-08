(ns childrensfutures-trade.core
  (:require
   [cljs-time.extend]
   [cljsjs.material-ui]
   [cljsjs.react-flexbox-grid]
   [cljsjs.web3]
   [childrensfutures-trade.automato.handlers]
   [childrensfutures-trade.automato.subs]
   [childrensfutures-trade.automato.views :as views]
   ;; [childrensfutures-trade.views :as views]
   [childrensfutures-trade.routes :refer [routes]]
   [print.foo.preloads.devtools]
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [reagent.core :as reagent]
   [pushy.core :as pushy]
   [bidi.bidi :as bidi]
   ))


(enable-console-print!)

(def history
  (pushy/pushy #(dispatch [:set-current-page %]) (partial bidi/match-route routes)))

(defn mount-root []
  (reagent/render [views/slides-view]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize])
  (pushy/start! history)
  (mount-root))
