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

   ))



(def default-chat-open-button-title "Discuss details")

;;;
;;; card extra subtitle
;;;
(defn investment-subtitle [goal]
  (let [selected-bid (subscribe [:db.goal.bids/selected (:goal-id goal)])]
    [:span "Investment: " (:description @selected-bid)]))

;;;
;;; simple card title
;;;
(defn simple-card-title [title]
  [ui/card-title
   {:title title}])

;;;
;;; card button
;;;
(defn card-flat-button [props]
  [ui/flat-button
   (r/merge-props
    {:secondary true
     :style st/goal-card-button}
    props)])

(defn card-raised-button [props]
  [ui/raised-button
   (r/merge-props
    {:secondary false
     :style st/goal-card-button}
    props)])

;;;
;;; chat button
;;;
(defn chat-open-button
  ([]
   (chat-open-button default-chat-open-button-title))

  ([title]
   [card-raised-button
   {:secondary true
    :label title
    :on-touch-tap #(dispatch [:ui.chat/toggle-view])}]))


;;;
;;; empty actions
;;;
(def empty-actions (constantly []))
