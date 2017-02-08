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
    [clavatar-js.core :as clavatar]

    [childrensfutures-trade.components.avatars :refer [bid-avatar]]
    [childrensfutures-trade.components.goal.utils :as gu]
    [childrensfutures-trade.components.goal.common :as gc]))


;;;
;;;
;;; goal actions
;;;
;;;
(defn- owner-actions [goal]
  (let [{:keys [goal-id cancelled? trx-on-air?]} goal]
    [
     ^{:key :cancel-goal}
     [gc/card-flat-button
      {:secondary true
       :disabled (or cancelled?
                     trx-on-air?)
       :label "Delete"
       :on-touch-tap #(dispatch [:cancel-goal/send goal-id])}]]))

(defn- bid-owner-actions [goal]
  [])

(defn- stranger-actions [goal]
  (let [{:keys [goal-id]} goal]
    [
     ^{:key :invest-now}
     [gc/card-raised-button
      {:secondary true
       :disabled false
       :label "Invest Now"
       :on-touch-tap #(dispatch [:place-bid/show-new-bid goal-id])}]]))


;; (defn- card-actions [goal]
;;   (gu/with-role
;;     goal
;;     {:goal-owner (owner-actions goal)
;;      :bid-owner (bid-owner-actions goal)}
;;     (stranger-actions goal)))

;; (defn- card-actions [goal]
;;   (let [goal-id (:goal-id goal)
;;         role (subscribe [:role/role goal-id])]
;;     (condp = @role
;;       :goal-owner (owner-actions goal)
;;       :bid-owner (bid-owner-actions goal)
;;       (stranger-actions goal))))

;;;
;;;
;;; card text
;;;
;;;


;;;
;;; goal owner
;;;

;;; card text
(defn- owner-card-text [goal]
  (let [{:keys [goal-id]} goal
        bids (subscribe [:db.goal.bids/sorted goal-id])]
    [:div
     [gc/simple-card-title "Select your investment!"]
     [gc/goal-bids-view goal]]))


;;; bid owner
(defn- bid-owner-card-text [goal]
  [gc/simple-card-title
   "You’ve made it! Now wait for goal owner's decision!"])

;;; stranger
(defn- stranger-card-text [goal]
  [gc/simple-card-title
   "Don’t wait! Invest now!"])

(defn card-text [goal]
  (gu/with-role
    goal
    {:goal-owner [owner-card-text goal]
     :bid-owner [bid-owner-card-text goal]}
    [stranger-card-text goal]))

;; (defn card-text [goal]
;;   (let [goal-id (:goal-id goal)
;;         role (subscribe [:role/role goal-id])]
;;     (condp = @role
;;       :goal-owner [owner-card-text goal]
;;       :bid-owner [bid-owner-card-text goal]
;;       [stranger-card-text goal])))

;;;
;;; card extra subtitle
;;;
;; (defn- card-subtitle-extra [goal]
;;   (let [goal-id (:goal-id goal)
;;         bids (subscribe [:db.goal.bids/sorted goal-id])]
;;     [:span
;;      (count @bids) " potential investors"]))

;;;
;;; goal chips
;;;
(defn- goal-statuses-extra [goal]
  (let [goal-id (:goal-id goal)
        bids (subscribe [:db.goal.bids/sorted goal-id])
        content (str (count @bids) " potential investor")
        content (if (= (count @bids) 1)
                  content
                  (str content "s"))]
    [{:key :investors-count
      :content content}]))
;;;
;;;
;;; PROPERTIES
;;;
;;;
(def card-properties
  (gu/card-properties
   {:card-style (constantly st/goal-card)
    :card-text card-text
    :card-actions #(gu/with-role %
                     {:goal-owner (owner-actions %)
                      :bid-owner (bid-owner-actions %)}
                     ;; default
                     (stranger-actions %))
    :goal-statuses-extra goal-statuses-extra}))
