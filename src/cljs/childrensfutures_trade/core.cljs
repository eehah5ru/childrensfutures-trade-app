(ns childrensfutures-trade.core
  (:require
   [cljs-time.extend]
   [cljsjs.material-ui]
   [cljsjs.react-flexbox-grid]
   ;; [cljsjs.web3]
   [madvas.re-frame.google-analytics-fx]
   [childrensfutures-trade.handlers]
   [childrensfutures-trade.subs]
   [childrensfutures-trade.views :as views]
   [childrensfutures-trade.pages :refer [routes]]
   [childrensfutures-trade.utils :as u]
   [print.foo.preloads.devtools]
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [reagent.core :as reagent]
   [pushy.core :as pushy]
   [bidi.bidi :as bidi]
   [madvas.re-frame.google-analytics-fx :as google-analytics-fx]


   ))



(enable-console-print!)

(def history
  (pushy/pushy #(dispatch [:ui.set-current-page %]) (partial bidi/match-route routes)))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []

  (when (not childrensfutures-trade.utils.DEV)
    (google-analytics-fx/set-enabled! (not childrensfutures-trade.utils.DEV)))

  (.addEventListener js/window
                     "load"
                     (fn []
                       (re-frame/dispatch-sync [:initialize])
                       (pushy/start! history)
                       (mount-root)))
  (.addEventListener js/window "resize" #(dispatch [:ui.window/resize])))
