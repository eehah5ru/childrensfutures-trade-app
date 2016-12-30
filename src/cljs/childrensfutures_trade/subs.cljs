(ns childrensfutures-trade.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))



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
 :db/goals
 ;; (fn [db]
 ;;   (sort-by :date #(compare %2 %1) (:tweets db)))
 ;; TODO: add sort by date???
 (fn [db]
   (sort-by :created-at (vals (:goals db)))))



(reg-sub
 :db/new-goal
 (fn [db]
   (:new-goal db)))

;; (reg-sub
;;   :db/settings
;;   (fn [db]
;;     (:settings db)))

(reg-sub
 :new-goal/selected-address-balance
 (fn [db]
   (get-in db [:accounts (:address (:new-goal db)) :balance])))
