(ns childrensfutures-trade.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))


;;;
;;;
;;; ADDRESSES AND BALANCES
;;;
;;;
(reg-sub
 :db/my-addresses
 (fn [db]
   (:my-addresses db)))

(reg-sub
 :db/current-address
 :<- [:db/my-addresses]
 (fn [my-addresses _]
   (first my-addresses)))

(reg-sub
 :new-goal/selected-address-balance
 (fn [db]
   (get-in db [:accounts (:address (:new-goal db)) :balance])))


;;;
;;;
;;; GOAL RELATED
;;;
;;;

;;;
;;; sorted goals
;;;
(reg-sub
 :db/goals
 (fn [db]
   (sort-by :created-at (vals (:goals db)))))


;;;
;;; new goal
;;;
(reg-sub
 :db/new-goal
 (fn [db]
   (:new-goal db)))

;;;
;;; new bid for goal
;;;
(reg-sub
 :db/new-bid
 (fn [db [_ goal-id]]
   (get-in db [:goals goal-id :new-bid])))


;;;
;;; show new bid indicator
;;;
(reg-sub
 :db/show-new-bid?
 (fn [db [_ goal-id]]
   (get-in db [:goals goal-id :show-new-bid?])))
