(ns childrensfutures-trade.handlers.place-bid
  (:require
   [ajax.core :as ajax]
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs-web3.personal :as web3-personal]
   ;; [cljsjs.web3]
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
   [childrensfutures-trade.handlers.utils :refer [goal-gas-limit]]
   [childrensfutures-trade.handlers.interceptors :refer [interceptors
                                                         interceptors-fx]]

   [childrensfutures-trade.handlers.utils :as hu]
   ))


;;;
;;;
;;; PLACE BID
;;;
;;;

;;;
;;; make place bid trx in the ethereum
;;;
(reg-event-fx
 :blockchain.place-bid/send
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [goal-id]]
   (let [{:keys [description]} (:new-bid db)]
     (hu/blockchain-send-transaction
            db
            :gse-contract
            :place-bid
            [goal-id description]
            :db-path [goal-id :place-bid]
            :confirmed-event [:blockchain.goal.place-bid/transaction-confirmed goal-id]
            :error-event :log-error
            :receipt-loaded-event [:blockchain.goal.place-bid/transaction-receipt-loaded goal-id]))))


;;;
;;; change state of placed bid
;;; if trx was confirmed by user
;;;
(reg-event-fx
 :blockchain.goal.place-bid/transaction-confirmed
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [goal-id tx-hash]]
   {:db (-> db
            (assoc-in [:goals goal-id :trx-on-air?] true)
            (assoc-in [:new-bid :placing?] true))
    :dispatch [:ui.snackbar/show "Wait while bid is being saved in blockchain"]}))

;;;
;;; confirms that bid was placed
;;;
(reg-event-fx
 :blockchain.goal.place-bid/transaction-receipt-loaded
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [goal-id & {:keys [gas-used] :as transaction-receipt}]]
   (console :log transaction-receipt)
   (when (= gas-used goal-gas-limit)
     (console :error "All gas used"))
   {:db (-> db
       (assoc-in [:goals goal-id :trx-on-air?] false)
       (assoc-in [:new-bid :placing?] false))
    :dispatch [:ui.snackbar/show-updating]}))
