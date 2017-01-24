(ns childrensfutures-trade.components.goal.bid-placed
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
    [clavatar-js.core :as clavatar]))


;;;
;;; goal actions
;;;
(defn- owner-actions [goal]
  (let [{:keys [goal-id cancelled? trx-on-air?]} goal]
    [
     ^{:key :cancel-goal}
     [ui/flat-button
      {:secondary true
       :disabled (or cancelled?
                     trx-on-air?)
       :label "Delete"
       :on-touch-tap #(dispatch [:cancel-goal/send goal-id])}]]))

(defn- bid-owner-actions [goal]
  (let [{:keys [goal-id]} goal]
    [
     ^{:key :open-chat}
     [ui/raised-button
      {:secondary true
       :disabled false
       :label "Discuss details"
       :on-touch-tap #(dispatch [:ui.chat/open goal-id])}]]))

(defn- stranger-actions [goal]
  (let [{:keys [goal-id]} goal]
    [
     ^{:key :invest-now}
     [ui/raised-button
      {:secondary true
       :disabled false
       :label "Invest Now"
       :on-touch-tap #(dispatch [:place-bid/show-new-bid goal-id])}]]))

(defn- card-actions [goal]
  (let [goal-id (:goal-id goal)
        role (subscribe [:role/role goal-id])]
    (condp = @role
      :goal-owner (owner-actions goal)
      :bid-owner (bid-owner-actions goal)
      (stranger-actions goal))))

;;;
;;; card text
;;;
(defn- owner-card-text [goal]
  [ui/card-title
   {:title "Select your investment!"}
   [:h1 "investments are here"]])

(defn- bid-owner-card-text [goal]
  [ui/card-title
   {:title "Wait for goal owner's decision"}])

(defn- stranger-card-text [goal]
  [:p "Don't wait! invest now!"])

(defn card-text [goal]
  (let [goal-id (:goal-id goal)
        role (subscribe [:role/role goal-id])]
    (condp = @role
      :goal-owner [owner-card-text goal]
      :bid-owner [bid-owner-card-text goal]
      [stranger-card-text goal])))

;;;
;;; PROPERTIES
;;;
(def card-properties
  {:card-style (constantly st/goal-card)
   :card-text card-text
   :card-actions card-actions})
