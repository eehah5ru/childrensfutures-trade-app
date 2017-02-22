(ns childrensfutures-trade.handlers.select-bid
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
   [childrensfutures-trade.handlers.utils :refer [goal-gas-limit]]
   [childrensfutures-trade.handlers.interceptors :refer [interceptors
                                                         interceptors-fx]]
   ))

;;;
;;;
;;; SELECT BID
;;;
;;;

;;;
;;; make select bid trx in the ethereum
;;;
(reg-event-fx
 :blockchain.select-bid/send
 (interceptors-fx :spec false)
 (fn [{:keys [db]} [goal-id bid-id]]
   (let [address (:current-address db)
         contract-instance (:instance (:gse-contract db))]
     {:web3-fx.contract/state-fn
      {:instance contract-instance
       :web3 (:web3 db)
       :db-path [:gse-contract :select-bid (keyword goal-id) (keyword bid-id)]
       :fns [[contract-instance
              :select-bid goal-id bid-id
              {:from address
               :gas goal-gas-limit}
              [:blockchain.select-bid/confirmed goal-id bid-id]
              :log-error
              [:blockchain.select-bid/transaction-receipt-loaded goal-id bid-id]]]}})))

;;;
;;; change state of selected bid
;;; if trx was confirmed by user
;;;
(reg-event-db
 :blockchain.select-bid/confirmed
 (interceptors)
 (fn [db [goal-id bid-id tx-hash]]
   (assoc-in db [:goals goal-id :bids bid-id :selecting?] true)))

;;;
;;; confirms that bid was selected
;;;
(reg-event-db
 :blockchain.select-bid/transaction-receipt-loaded
 (interceptors)
 (fn [db [goal-id bid-id & {:keys [gas-used] :as transaction-receipt}]]
   (console :log transaction-receipt)
   (when (= gas-used goal-gas-limit)
     (console :error "All gas used"))
   (-> db
       (assoc-in [:goals goal-id :bids bid-id :selecting?] false))))
