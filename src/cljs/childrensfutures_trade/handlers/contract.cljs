(ns childrensfutures-trade.handlers.contract
  (:require
   [ajax.core :as ajax]
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs-web3.personal :as web3-personal]
   [cljsjs.web3]
   [cljs.spec :as s]
   [childrensfutures-trade.db :as db]
   [day8.re-frame.http-fx]
   [goog.string :as gstring]
   [goog.string.format]
   [madvas.re-frame.web3-fx]
   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console dispatch]]
   [childrensfutures-trade.utils :as u]

   ;;
   ;; event handlers
   ;;
   [childrensfutures-trade.handlers.interceptors :refer [interceptors
                                                         interceptors-fx]]
   ))


;;;
;;;
;;; register handlers for contract events
;;;
;;;
(reg-event-fx
 :contract/abi-loaded
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [abi]]
   (let [web3 (:web3 db)
         contract-instance (web3-eth/contract-at web3 abi (:address (:contract db)))]
     {:db (assoc-in db [:contract :instance] contract-instance)

      :web3-fx.contract/events
      {:instance contract-instance
       :db db
       :db-path [:contract :events]
       :events [[:GoalAdded {} {:from-block 0} :contract/on-goal-loaded :log-error]
                [:GoalCancelled {} {:from-block 0} :contract/on-goal-cancelled :log-error]
                [:BidPlaced {} {:from-block 0} :contract/on-bid-placed :log-error]
                [:BidSelected {} {:from-block 0} :contract/on-bid-selected :log-error]]}


      ;; :web3-fx.contract/constant-fns
      ;; {:instance contract-instance
      ;;  :fns [[:get-settings :contract/settings-loaded :log-error]]}
      })))


;;;
;;;
;;; ETHEREUM EVENTS
;;;
;;;

;;;
;;;
;;; event for GoalAdded contract event
;;;
;;;
(reg-event-db
 :contract/on-goal-loaded
 interceptors
 (fn [db [goal]]
   (assoc-in db [:goals (:goal-id goal)] (merge (db/default-goal)
                                                (select-keys goal [:owner :description :goal-id])))))



;;;
;;;
;;; GoalCancelled contract event
;;;
;;;
(reg-event-db
 :contract/on-goal-cancelled
 interceptors
 (fn [db [goal]]
   (js/console.log :debug :on-goal-cancelled (:goal-id goal))
   (assoc-in db [:goals (:goal-id goal) :cancelled?] true)))


;;;
;;;
;;; BidPlaced contract event
;;;
;;;
(reg-event-db
 :contract/on-bid-placed
 interceptors
 (fn [db [bid]]
   (js/console.log :info :bid-placed bid)
   (assoc-in db
             [:goals (:goal-id bid) :bids (:bid-owner bid)] ; FIXME: bid-owner -> bid-id
             (merge (db/default-bid)
                    (let [{:keys [bid-owner description goal-id]} bid]
                      {:goal-id goal-id
                       :owner bid-owner
                       :description description})))))


;;;
;;;
;;; BidSelected
;;;
;;;
(reg-event-db
 :contract/on-bid-selected
 interceptors
 (fn [db [{:keys [goal-id bid-id]}]]
   (js/console.log :info :bid-selected goal-id bid-id)
   (assoc-in db
             [:goals goal-id :bids bid-id :selected?]
             true)))
