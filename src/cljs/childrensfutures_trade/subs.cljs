(ns childrensfutures-trade.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [cemerick.url :as url]
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
  :gse-contract/active-state?
  (fn [db]
    (= :active (get-in db [:gse-contract :state]))))

;;;
;;; is web3 available
;;;
(reg-sub
 :contract/web3-available?
 (fn [db]
   (:provides-web3? db)))

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
               :stranger))))
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
;;; get specific goal
;;;
(reg-sub
 :db.goals/get
 :<- [:db/goals]

 (fn [goals [_ goal-id]]
   (get goals goal-id)))

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
 :db.goal/my?
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

;;;
;;;
;;; CHAT
;;;
;;;
(reg-sub
 :db.chat/new-chat-message
 (fn [db]
   (:new-chat-message db)))

(reg-sub
 :db.chat/current-channel-id
 (fn [db]
   (:current-chat-channel-id db)))

(reg-sub
 :db.chats/all
 (fn [db]
   (:messages db)))

(reg-sub
 :db.chat.current/messages
 :<- [:db.chat/current-channel-id]
 :<- [:db.chats/all]

 (fn [[channel-id chats] _]
   (get chats channel-id [])))

;;;
;;;
;;; PULSE
;;;
;;;
(reg-sub
 :db.pulse/all-events

 (fn [db]
   (sort-by :number #(compare %2 %1) (:pulse db))))


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

(reg-sub
 :ui.goal/show-share-url?
 :<- [:db/goals]

 (fn [goals [_ goal-id]]
   (get-in goals [goal-id :show-share-url?])))


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
 :ui.chat/drawer-open?
 (fn [db]
   (:chat-open? db)))

;;;
;;; page subs
;;;
(reg-sub
 :ui/current-page
 (fn [db]
   (:current-page db)))

(reg-sub
 :ui/current-page-name
 :<- [:ui/current-page]
 (fn [current-page _]
   (pages/human-readable (:handler current-page))))

;;;
;;; select bid dialog  subs
;;;
(reg-sub
 :ui.select-bid/dialog-open?
 (fn [db]
   (get-in db [:select-bid :dialog-open?])))

(reg-sub
 :ui.select-bid/selected
 (fn [db]
   (-> db
       :select-bid
       (select-keys [:goal-id :bid-id]))))

;;;
;;; view goal dialog subs
;;;
(reg-sub
 :ui.view-goal/dialog-open?
 (fn [db]
   (get-in db [:view-goal :dialog-open?])))

(reg-sub
 :ui.view-goal/goal-id
 (fn [db]
   (get-in db [:view-goal :goal-id])))

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
;;; get proto-host-port
;;;
(reg-sub
 :location/root
 (fn [_]
   (str (url/url (-> js/window .-location .-href) "/"))))
