(ns childrensfutures-trade.components.goal.common
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

   [childrensfutures-trade.components.avatars :refer [bid-avatar]]
   [childrensfutures-trade.components.goal.utils :as gu]

   [childrensfutures-trade.components.chat :refer [chat-open-button]]))


;;;
;;; card extra subtitle
;;;
(defn investment-subtitle [goal]
  (let [selected-bid (subscribe [:db.goal.bids/selected (:goal-id goal)])]
    [:span "Investment: " (:description @selected-bid)]))


;;;
;;; empty actions
;;;
(def empty-actions (constantly []))
