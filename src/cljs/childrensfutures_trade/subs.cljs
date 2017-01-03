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
;;; goals
;;; @returns map
;;;
(reg-sub
 :db/goals
 (fn [db]
   (:goals db)))

;;;
;;; sorted goals
;;; @returns sequence
;;;
(reg-sub
 :db/sorted-goals
 :<- [:db/goals]
 (fn [goals _]
   (sort-by :created-at (vals goals))))


;;;
;;; is it my goal?
;;; @returns boolean
;;;
(reg-sub
 :db/my-goal?
 :<- [:db/current-address]
 :<- [:db/goals]
 (fn [[current-address goals] [_ goal-id]]
   (= current-address
      (get-in goals [goal-id :owner]))))

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
;;; bids for goal
;;;
(reg-sub
 :db/bids
 (fn [db [_ goal-id]]
   (get-in db [:goals goal-id :bids])))


;;;
;;; bids for goal as sorted vector
;;;
(reg-sub
 :db/sorted-bids

 ;; input
 (fn [[_ goal-id] _]
   (subscribe [:db/bids goal-id]))

 ;; reaction
 (fn [bids _]
   (sort-by :created-at (vals bids)))
 )

(reg-sub
 :db/already-bidded?
 ;; input
 (fn [[_ goal-id] _]
   [(subscribe [:db/current-address])
    (subscribe [:db/bids goal-id])])

 ;; reaction
 (fn [[current-address bids] _]
   (contains? bids
              current-address))
 )

;;;
;;; show new bid indicator
;;;
(reg-sub
 :db/show-new-bid?
 (fn [db [_ goal-id]]
   (get-in db [:goals goal-id :show-new-bid?])))
