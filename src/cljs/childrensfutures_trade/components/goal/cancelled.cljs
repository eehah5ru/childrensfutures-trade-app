(ns childrensfutures-trade.components.goal.cancelled
  (:require
   [cljs-react-material-ui.icons :as icons]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.core :refer [color]]
   [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
   [childrensfutures-trade.styles :as st]
   [childrensfutures-trade.utils :as u]
   [medley.core :as medley]
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as r]
   [clavatar-js.core :as clavatar]

   [childrensfutures-trade.goal-stages :as gs]
   [childrensfutures-trade.components.goal.utils :as gu]))


(defn card-text [goal]
  [gu/card-title
   {:title "It's sad to say but this dream has passed away!"}])


(def card-properties
  (gu/card-properties
   {:card-style (constantly st/goal-card-cancelled)
    :card-text card-text
    :card-actions (constantly [])}))
