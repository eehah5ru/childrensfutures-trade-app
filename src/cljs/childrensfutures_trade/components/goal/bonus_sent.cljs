(ns childrensfutures-trade.components.goal.bonus-sent
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
   [childrensfutures-trade.components.goal.common :as gc]

   ))

(defn- complete-goal-button [goal]
  (let [goal-id (:goal-id goal)
        selected-bid (subscribe [:db.goal.bids/selected goal-id])
        bid-id (:bid-id @selected-bid)]
    [gc/card-raised-button
     {:primary true
      :label "Thanks!"
      :on-touch-tap #(dispatch
                      [:blockchain.complete-goal/send goal-id bid-id])}]))

;;;
;;; actions
;;;

;;; owner
(defn- owner-actions [goal]
  [
   [gc/owner-investor-chat-button (:goal-id goal)]])

;;; investor
(defn- investor-actions [goal]
  [
   [complete-goal-button goal]
   [gc/owner-investor-chat-button (:goal-id goal)]])


;;;
;;; card text
;;;
(defn- card-text [goal]
  (gu/with-role
    goal
    {:goal-owner [gc/simple-card-title
                  "Wait while the bonus is on the way!"]
     :investor [gc/simple-card-title
                "Got it? Say thanks!"]}
    [gc/simple-card-title
     "Let's wait for the incredible moments! Or work on your future NOW!"]))


;;;
;;;
;;; PROPERTIES
;;;
;;;
(def card-properties
  (gu/card-properties
   {:card-text card-text
    :card-actions #(gu/with-role %
                     {:goal-owner (owner-actions %)
                      :investor (investor-actions %)}
                     ;; default
                     [])

    :card-subtitle-extra gc/investment-subtitle}))
