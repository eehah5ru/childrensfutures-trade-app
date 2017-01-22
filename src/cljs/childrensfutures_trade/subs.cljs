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
 :db/selected-address-balance
 (fn [db]
   (get-in db [:accounts (:current-address db) :balance])))


;;;
;;;
;;; CONTRACT
;;;
;;;
(reg-sub
  :contract/active-state?
  (fn [db]
    (= :active (get-in db [:contract :state]))))


;;;
;;;
;;; UI RELATED
;;;
;;;
(reg-sub
 :ui/show-new-goal?
 (fn [db]
   (:show-new-goal? db)))

(reg-sub
 :ui/show-new-bid?
 (fn [db]
   (:show-new-bid? db)))

;;; FIXME: deprecated
(reg-sub
 :ui/show-accounts?
 (fn [db]
   (:show-accounts? db)))

(reg-sub
 :ui/drawer-open?
 (fn [db]
   (:drawer-open? db)))

(reg-sub
 :ui/current-page
 (fn [db]
   (:current-page db)))

;;;
;;; show new bid indicator
;;;
;; (reg-sub
;;  :db/show-new-bid?
;;  (fn [db [_ goal-id]]
;;    (get-in db [:goals goal-id :show-new-bid?])))


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
   (sort-by :created-at #(compare %2 %1) (vals goals))))

;;;
;;; get num goals for account
;;;
(reg-sub
 :db.goals/count
 :<- [:db/goals]
 (fn [goals [_ owner]]
   (js/console.log owner)
   (count (filter #(= (:owner %) owner) (vals goals)))))

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

(reg-sub
 :db/sending-new-goal?
 :<- [:db/new-goal]
 (fn [new-goal _]
   (:sending? new-goal)))


;;;
;;;
;;; BIDS
;;;
;;;

;;;
;;; new bid for goal
;;;
(reg-sub
 :db/new-bid
 (fn [db]
   (:new-bid db)))

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

;;;
;;; returns true if current goal already bidded by user
;;;
(reg-sub
 :db/already-bidded?
 ;; input
 (fn [[_ goal-id] _]
   [(subscribe [:db/current-address])
    (subscribe [:db/bids goal-id])])

 ;; reaction
 (fn [[current-address bids] _]
   (contains? bids
              current-address)))


;;;
;;; is it my bid?
;;; @returns boolean
;;;
(reg-sub
 :db/my-bid?

 ;; input
 (fn [[_ goal-id bid-id] _]
   [(subscribe [:db/current-address])
    (subscribe [:db/bids goal-id])])

 ;; reaction
 (fn [[current-address bids] [_ goal-id bid-id]]
   (= current-address
      (get-in bids [bid-id :owner]))))

;;;
;;; returns true if goal has already selected bid
;;;
(reg-sub
 :db/goal-has-selected-bid?

 ;; input
 (fn [[_ goal-id] _]
   (subscribe [:db/bids goal-id]))

 ;; reaction
 (fn [bids _]
   false))
