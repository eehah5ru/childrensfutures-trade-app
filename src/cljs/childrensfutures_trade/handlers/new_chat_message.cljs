(ns childrensfutures-trade.handlers.new-chat-message
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
   ;; [childrensfutures-trade.utils :as u]

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
;;; NEW CHAT MESSAGE
;;;
;;;

;;;
;;;
;;; update new message values while editing message
;;;
;;;
(reg-event-db
 :new-chat-message.attribute/update
 (interceptors)
 (fn [db [key value]]
   (assoc-in db [:new-chat-message key] value)))


;;;
;;; send newchat message to ethereum chat contract
;;;
(reg-event-fx
 :blockchain.new-chat-message/send
 (interceptors-fx :spec false)

 (fn [{:keys [db]}]
   (js/console.log :debug :new-chat-message (:new-chat-message db))
   (let [{:keys [channel-id
                 owner
                 text]} (:new-chat-message db)]
     (hu/blockchain-send-transaction
      db
      :chat-contract
      :send-message
      [channel-id text]
      :db-path [:send-message]
      :confirmed-event [:blockchain.new-chat-message/confirmed]
      :error-event :log-error
      :receipt-loaded-event [:blockchain.new-chat-message/transaction-receipt-loaded]))))


;;;
;;; change new-chat-message's state to sending
;;; fired after the trx has been confirmed
;;; see event handler above
;;;
(reg-event-db
 :blockchain.new-chat-message/confirmed
 (interceptors)
 (fn [db [transaction-hash]]
   (-> db
       (assoc-in [:new-chat-message :trx-on-air?] true))))


;;;
;;; confirms that chat message was sent to ethereum contract
;;;
(reg-event-db
 :blockchain.new-chat-message/transaction-receipt-loaded
 (interceptors)
 (fn [db [{:keys [gas-used] :as transaction-receipt}]]
   (console :log transaction-receipt)
   (when (= gas-used goal-gas-limit)
     (console :error "All gas used"))
   (-> db
       (assoc :new-chat-message (db/default-chat-message))
       (assoc-in [:new-chat-message :owner] (:current-address db)))))
