(ns childrensfutures-trade.components.goal.created
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

   [childrensfutures-trade.components.goal.utils :as gu]
   [childrensfutures-trade.components.goal.common :as gc]))


;;;
;;; goal actions
;;;
(defn- owner-actions [goal]
  (let [{:keys [goal-id cancelled? trx-on-air?]} goal
        read-only-app? (subscribe [:app/read-only?])]
    [
     ^{:key :cancel-goal}
     [gc/card-flat-button
      {:secondary true
       :disabled (or cancelled?
                     trx-on-air?
                     @read-only-app?)
       :label "Delete"
       :on-touch-tap #(dispatch [:cancel-goal/send goal-id])}]]))

(defn- stranger-actions [goal]
  (let [{:keys [goal-id trx-on-air?]} goal
        read-only-app? (subscribe [:app/read-only?])]
    [
     ^{:key :invest-now}
     [gc/card-raised-button
      {:secondary true
       :disabled (or trx-on-air?
                     @read-only-app?)
       :label "Don’t wait! Invest now!"
       :on-touch-tap #(dispatch [:place-bid/show-new-bid goal-id])}]]))

(defn- card-actions [goal]
  (let [goal-id (:goal-id goal)
        role (subscribe [:role/role goal-id])]
    (condp = @role
      :goal-owner (owner-actions goal)
      (stranger-actions goal))))

;;;
;;; card text
;;;
(defn- owner-card-text [goal]
  [gu/card-title
   {:title "OK! Let's wait for bids! There are surely people who are interested in your dreams!"}])

(defn- stranger-card-text [goal]
  [gu/card-title
   {:title "Don't wait! invest now!"}])

(defn card-text [goal]
  (let [goal-id (:goal-id goal)
        role (subscribe [:role/role goal-id])]
    (condp = @role
      :goal-owner [owner-card-text goal]
      [stranger-card-text goal])))

;;;
;;; PROPERTIES
;;;
(def card-properties
  (gu/card-properties
   {:card-style (constantly st/goal-card)
    :card-text card-text
    :card-actions card-actions}))
