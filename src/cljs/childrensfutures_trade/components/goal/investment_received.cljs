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

   [childrensfutures-trade.components.chat :refer [chat-open-button]]))

(defn- goal-achieved-button [goal]
  (let [goal-id (:goal-id goal)]
    [ui/raised-button
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
   [chat-open-button]])

;;; investor
(defn- investor-actions [goal]
  [
   [chat-open-button]])


;;;
;;; card text
;;;
(defn- card-text [goal]
  (gu/with-role
    goal
    {:goal-owner [gc/simple-card-title
                  "As soon you've achieved the goal tell Investor about it! Or discuss details in chat!"]
     :investor [gc/simple-card-title
                "Discuss weather in chat while she is working hard!"]}
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
