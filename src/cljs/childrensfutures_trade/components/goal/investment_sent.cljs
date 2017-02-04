(ns childrensfutures-trade.components.goal.investment-sent
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

(defn- investment-received-button [goal]
  (let [goal-id (:goal-id goal)
        selected-bid (subscribe [:db.goal.bids/selected goal-id])
        bid-id (:bid-id @selected-bid)]
    [ui/raised-button
     {:primary true
      :label "Got Investment!"
      :on-touch-tap #(dispatch
                      [:blockchain.receive-investment/send goal-id bid-id])}]))

;;;
;;; actions
;;;

;;; owner
(defn- owner-actions [goal]
  [
   [investment-received-button goal]
   [gc/owner-investor-chat-button (:goal-id goal)]])

;;; investor
(defn- investor-actions [goal]
  [[gc/owner-investor-chat-button (:goal-id goal)]])


;;;
;;; card text
;;;
(defn- card-text [goal]
  (gu/with-role
    goal
    {:goal-owner [gc/simple-card-title
                  "As soon you've got an investment tell Investor about it! Or discuss details in chat!"]
     :investor [gc/simple-card-title
                "Discuss weather in chat while your investment is on the way!"]}
    [gc/simple-card-title
     "Let's wait for incredible moments! Or invest in your future now!"]))


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
