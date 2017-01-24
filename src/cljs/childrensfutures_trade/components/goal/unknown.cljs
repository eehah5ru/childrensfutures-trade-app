(ns childrensfutures-trade.components.goal.unknown
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

    [childrensfutures-trade.goal-stages :as gs]))

(defn unknown-goal-view [goal]
  (let [{:keys [description give-in-return]} goal]
    []))

(def card-properties
  {:card-style (constantly st/goal-card)
   :card-text (constantly [:h1 "Unknown goal"])
   :card-actions (constantly [])})
