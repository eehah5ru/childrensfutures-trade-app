(ns childrensfutures-trade.handlers.goal
  (:require
   [childrensfutures-trade.db :as db]
   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console dispatch]]
   [childrensfutures-trade.utils :as u]

   ;;
   ;; event handlers
   ;;
   [childrensfutures-trade.handlers.interceptors :refer [interceptors
                                                         interceptors-fx]]

   [childrensfutures-trade.handlers.utils :as hu]
   ))



;;;
;;;
;;; common contract events
;;;
;;;

;;;
;;; mark goal as on air
;;; if trx was confirmed by user
;;;
(reg-event-fx
 :blockchain.goal/transaction-confirmed
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [goal-id tx-hash]]
   ;; (js/console.log :trx-on-air goal-id true)
   {:db (assoc-in db [:goals goal-id :trx-on-air?] true)
    :dispatch [:ui.snackbar/show "Changes are being saved to blockhain. Please wait!"]}))


;;;
;;; confirms that bid was selected
;;;
(reg-event-fx
 :blockchain.goal/transaction-receipt-loaded
 (interceptors-fx :spec true)

 (hu/blockchain-trx-receipt-loaded-fx
  (fn [db [goal-id _]]
    ;; (js/console.log :trx-on-air goal-id false)
    {:db (-> db
             (assoc-in [:goals goal-id :trx-on-air?] false))
     :dispatch [:ui.snackbar/show-updating]})))

;;;
;;;
;;; goal lifecycle
;;;
;;;

;;;
;;; add goal
;;;
(reg-event-fx
 :db.goal/add
 (interceptors-fx :spec true)

 (fn [{:keys [db]} [goal created-at]]
   {:db (-> db
            (db/update-goal
             (:goal-id goal)
             #(merge % (select-keys goal [:owner
                                          :description
                                          :goal-id
                                          :give-in-return])
                     {:created-at created-at}))

            (db/change-stage
             (:goal-id goal)
             :created))
    :dispatch [:ui.snackbar/show "new goal added!"]}))

;;;
;;; cancel goal
;;;
(reg-event-fx
 :db.goal/cancel
 (interceptors-fx :spec true)

 (fn [{:keys [db]} [goal]]
   {:db (-> db
            (db/update-goal
             (:goal-id goal)
             #(merge % {:cancelled? true}))

            (db/change-stage (:goal-id goal) :cancelled))
    :dispatch [:ui.snackbar/show "goal cancelled!"]}))

;;;
;;; place bid
;;;
(reg-event-fx
 :db.goal/place-bid
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [bid]]
   {:db (-> db
            (db/change-stage (:goal-id bid) :bid-placed)

            (db/update-bid (:goal-id bid)
                           (:bid-owner bid)
                           #(merge %
                                   (let [{:keys [bid-owner description goal-id]} bid]
                                     {:goal-id goal-id
                                      :bid-id bid-owner ;FIXME use bid-id instead
                                      :owner bid-owner
                                      :description description}))))
    :dispatch [:ui.snackbar/show "someone has invested in the goal!"]}))


;;;
;;; select bid
;;;
(reg-event-fx
 :db.goal/select-bid
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [goal-id bid-id]]
   {:db (-> db
       (db/change-stage goal-id :bid-selected)

       (db/update-bid goal-id
                      bid-id
                      #(merge % {:goal-id goal-id
                                 :bid-id bid-id
                                 :selected? true})))
    :dispatch [:ui.snackbar/show "supporter was selected!"]}))


;;;
;;; send investment
;;;
(reg-event-fx
 :db.goal/send-investment
 (interceptors-fx :spec true)

 (fn [{:keys [db]} [goal-id bid-id]]
   {:db (db/change-stage db goal-id :investment-sent)
    :dispatch [:ui.snackbar/show "investment was sent!"]}))

;;;
;;; receive investment
;;;
(reg-event-fx
 :db.goal/receive-investment
 (interceptors-fx :spec true)

 (fn [{:keys [db]} [goal-id bid-id]]
   {:db (db/change-stage db goal-id :investment-received)
    :dispatch [:ui.snackbar/show "someone has just received some support!"]}))

;;;
;;; achieve goal
;;;
(reg-event-fx
 :db.goal/achieve
 (interceptors-fx :spec true)

 (fn [{:keys [db]} [goal-id]]
   {:db (db/change-stage db goal-id :goal-achieved)
    :dispatch [:ui.snackbar/show "Wow! Goal has been just achieved!"]}))

;;;
;;; ask bonus
;;;
(reg-event-fx
 :db.goal/ask-bonus
 (interceptors-fx :spec true)

 (fn [{:keys [db]} [goal-id bid-id]]
   {:db (db/change-stage db goal-id :bonus-asked)
    :dispatch [:ui.snackbar/show "Anonymous has just asked for her bonus!"]}))

;;;
;;; send bonus
;;;
(reg-event-fx
 :db.goal/send-bonus
 (interceptors :spec true)

 (fn [{:keys [db]} [goal-id bid-id]]
   {:db (db/change-stage db goal-id :bonus-sent)
    :dispatch [:ui.snackbar/show "Bonus was sent to anonymous!"]}))

;;;
;;; complete goal
;;;
(reg-event-fx
 :db.goal/complete
 (interceptors-fx :spec true)

 (fn [{:keys [db]} [goal-id]]
   {:db (db/change-stage db goal-id :goal-completed)
    :dispatch [:ui.snackbar/show "Yeap! Anonymous has just completed her goal!"]}))
