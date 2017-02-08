(ns childrensfutures-trade.components.goal.investment-received
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

(defn- goal-achieved-button [goal]
  (let [goal-id (:goal-id goal)]
    [gc/card-raised-button
     {:primary true
      :label "Goal is Achieved!"
      :on-touch-tap #(dispatch
                      [:blockchain.achieve-goal/send goal-id])}]))

;;;
;;; actions
;;;

;;; owner
(defn- owner-actions [goal]
  [
   [goal-achieved-button goal]
   [gc/owner-investor-chat-button (:goal-id goal)]])

;;; investor
(defn- investor-actions [goal]
  [
   [gc/owner-investor-chat-button (:goal-id goal)]])


;;;
;;; card text
;;;
(defn- card-text [goal]
  (gu/with-role
    goal
    {:goal-owner [gc/simple-card-title
                  "As soon as you achieve the goal tell the investor about it!"]
     :investor [gc/simple-card-title
                "Discuss the weather in a chat while the dreamer is working hard!"]}
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
