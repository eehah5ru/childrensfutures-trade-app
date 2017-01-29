(ns childrensfutures-trade.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [childrensfutures-trade.goal-stages :as gs]

            [childrensfutures-trade.pages :as pages]))


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
   ;; (js/console.log :debug :current-address (first my-addresses))
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
;;; FIXME: unused
(reg-sub
  :contract/active-state?
  (fn [db]
    (= :active (get-in db [:contract :state]))))

;;;
;;; is web3 available
;;;
(reg-sub
 :contract/web3-available?
 (fn [db]
   (:provides-web3? db)))

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
;;;
;;;
(reg-sub
 :ui/current-page-name
 :<- [:ui/current-page]
 (fn [current-page _]
   (pages/human-readable (:handler current-page))))

;;;
;;; window height
;;;
(reg-sub
 :ui/window-height
 (fn [db]
   (:window-height db)))
;;;
;;; show new bid indicator
;;;
;; (reg-sub
;;  :db/show-new-bid?
;;  (fn [db [_ goal-id]]
;;    (get-in db [:goals goal-id :show-new-bid?])))


;;;
;;;
;;; ROLES IN APP
;;;
;;;

;;;
;;; am I a goal owner?
;;;
(reg-sub
 :role/goal-owner?
 :<- [:db/current-address]
 :<- [:db/goals]

 (fn [[current-address goals] [_ goal-id]]
   ;; (js/console.log :debug :goal (get goals goal-id))
   (= current-address
      (get-in goals [goal-id :owner]))))

;;;
;;; am I bid owner?
;;;
(reg-sub
 :role/bid-owner?
 :<- [:db/current-address]
 :<- [:db/goals]

 (fn [[current-address goals] [_ goal-id]]
   (let [stage (get-in goals [goal-id :stage])]
     (and (gs/bid-placed? stage)
          (some #(= current-address
                    (:owner %))
                (-> goals
                    (get-in [goal-id :bids])
                    vals))))))

;;;
;;; am I an investor in the goal?
;;;
(reg-sub
 :role/investor?
 :<- [:db/current-address]
 :<- [:db/goals]

 (fn [[current-address goals] [_ goal-id]]
   (some #(and (= current-address
                  (:owner %))
               (:selected? %))
         (-> goals
             (get-in [goal-id :bids])
             vals))))

;;;
;;; am I a stranger?
;;;
(reg-sub
 :role/stranger?
 ;;
 ;; input
 ;;
 (fn [[_ goal-id] _]
   [(subscribe [:role/goal-owner? goal-id])
    (subscribe [:role/bid-owner? goal-id])
    (subscribe [:role/investor? goal-id])
    (subscribe [:db.goal/stage goal-id])])

 ;;
 ;; reaction
 ;;
 (fn [[goal-owner? bid-owner? investor? stage] _]
   (or (and (not goal-owner?)
            (not bid-owner?)
            (not investor?))
       (= stage :cancelled))))


;;;
;;;
;;; get my curent role
;;;
;;;
(reg-sub
 :role/role
 ;;
 ;;  input
 ;;
 (fn [[_ goal-id] _]
   [(subscribe [:role/goal-owner? goal-id])
    (subscribe [:role/bid-owner? goal-id])
    (subscribe [:role/investor? goal-id])
    (subscribe [:role/stranger? goal-id])])

 ;;
 ;; reaction
 ;;
 (fn [[goal-owner? bid-owner? investor? stranger?] _]
   ;; (js/console.log :debug :getting-role goal-owner?)
   (cond
     goal-owner? :goal-owner
     bid-owner? :bid-owner
     investor? :investor
     stranger? :stranger
     :else (do (js/console.log :warning "strange user role")
               :stranger)))
 )
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
 :db.goals/sorted
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
   (count (filter #(= (:owner %) owner) (vals goals)))))

;;;
;;; goal's stage
;;;
(reg-sub
 :db.goal/stage
 :<- [:db/goals]

 (fn [goals [_ goal-id]]
   (get-in goals [goal-id :stage])))

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
;;;
;;; sorted goals for current acccount
;;;
;;;
(reg-sub
 :db.goals.my/sorted
 :<- [:db/current-address]
 :<- [:db.goals/sorted]
 (fn [[current-address goals] _]
   (filter #(= (:owner %) current-address) goals)))

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
   (:trx-on-air? new-goal)))


;;;
;;;
;;; BIDS
;;;
;;;

;;;
;;; new bid for goal
;;;
(reg-sub
 :db.bids/new-bid
 (fn [db]
   (:new-bid db)))

;;;
;;; bids for goal
;;;
(reg-sub
 :db.goal.bids/all
 (fn [db [_ goal-id]]
   (get-in db [:goals goal-id :bids])))


;;;
;;; bids for goal as sorted vector
;;;
(reg-sub
 :db.goal.bids/sorted

 ;; input
 (fn [[_ goal-id] _]
   (subscribe [:db.goal.bids/all goal-id]))

 ;; reaction
 (fn [bids _]
   (sort-by :created-at (vals bids)))
 )

;;;
;;; selected bid
;;;
(reg-sub
 :db.goal.bids/selected
 ;; input
 (fn [[_ goal-id] _]
   (subscribe [:db.goal.bids/sorted goal-id]))

 ;; reaction
 (fn [bids _]
   (first (filter :selected? bids))))

;;;
;;; all bids for an account
;;;
(reg-sub
 :db.bids.my/sorted
 :<- [:db/current-address]
 :<- [:db.goals/sorted]

 (fn [[current-address goals] _]
   (let [all-bids (mapcat #(-> %
                               :bids
                               vals)
                          goals)]
     (filter #(= (:owner %) current-address)
             all-bids))))

;;;
;;; returns true if current goal already bidded by user
;;;
(reg-sub
 :db.goal/already-bidded?
 ;; input
 (fn [[_ goal-id] _]
   [(subscribe [:db/current-address])
    (subscribe [:db.goal.bids/all goal-id])])

 ;; reaction
 (fn [[current-address bids] _]
   (contains? bids
              current-address)))


;;;
;;; is it my bid?
;;; @returns boolean
;;;
(reg-sub
 :db.bids/my-bid?

 ;; input
 (fn [[_ goal-id bid-id] _]
   [(subscribe [:db/current-address])
    (subscribe [:db.goal.bids/all goal-id])])

 ;; reaction
 (fn [[current-address bids] [_ goal-id bid-id]]
   (= current-address
      (get-in bids [bid-id :owner]))))

;;;
;;; returns true if goal has already selected bid
;;;
(reg-sub
 :db.goal/has-selected-bid?

 ;; input
 (fn [[_ goal-id] _]
   (subscribe [:db.goal.bids/sorted goal-id]))

 ;; reaction
 (fn [bids _]
   (some #(= (:selected? %) true) bids)))
