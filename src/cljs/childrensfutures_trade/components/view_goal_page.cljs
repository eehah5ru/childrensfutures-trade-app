(ns childrensfutures-trade.components.view-goal-page
  (:require
    [cljs-react-material-ui.icons :as icons]
    [cljs-react-material-ui.reagent :as ui]
    [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
    [childrensfutures-trade.styles :as st]
    [childrensfutures-trade.utils :as u]
    [medley.core :as medley]
    [re-frame.core :refer [subscribe dispatch dispatch-sync]]
    [reagent.core :as r]


    [childrensfutures-trade.goal-stages :as gs]
    [childrensfutures-trade.components.pulse-page :refer [pulse-page]]
    ))

(defn ^:export  view-goal-page []
  (r/create-class
   {:component-did-mount #(dispatch-sync [:ui.view-goal-page/init])
    :display-name "view-goal-page"
    :reagent-render (fn []
                      [pulse-page])}))
