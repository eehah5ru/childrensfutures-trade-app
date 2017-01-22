(ns childrensfutures-trade.handlers.blockchain
  (:require
   [cljs.spec :as s]
   [goog.string :as gstring]
   [goog.string.format]
   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console dispatch]]

   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs-web3.personal :as web3-personal]
   [cljsjs.web3]
   [madvas.re-frame.web3-fx]

   [childrensfutures-trade.db :as db]
   [childrensfutures-trade.utils :as u]

   [childrensfutures-trade.handlers.interceptors :refer [interceptors
                                                         interceptors-fx]]
   ))

;;;
;;; load accounts from WEB3
;;;
(reg-event-fx
 :blockchain/load-my-addresses
 (interceptors-fx :spec false)
 (fn [{:keys [db]}]
   (when (:provides-web3? db)
     {:web3-fx.blockchain/fns
      {:web3 (:web3 db)
       :fns [[web3-eth/accounts [:blockchain/my-addresses-loaded] :log-error]]}})))


;;;
;;;
;;; when get access to ethereum node accounts
;;;
;;;
(reg-event-fx
 :blockchain/my-addresses-loaded
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [addresses]]
   {:db (-> db
            (assoc :my-addresses addresses)
            (assoc :current-address (first addresses)))
    :web3-fx.blockchain/balances {:web3 (:web3 db/default-db)
                                  :addresses addresses
                                  :watch? true
                                  :blockchain-filter-opts "latest"
                                  :dispatches [:blockchain/balance-loaded :log-error]}}))


;;;
;;;
;;; update balance
;;;
;;;
(reg-event-db
 :blockchain/balance-loaded
 interceptors
 (fn [db [balance address]]
   (assoc-in db [:accounts address :balance] balance)))

;;;
;;;
;;; select first address
;;;
;;;
(reg-event-db
 :blockchain.account/select-first
 interceptors
 (fn [db]
   (assoc db :current-address (first (:my-addresses db)))))


;;;
;;;
;;; update account info
;;;
;;;
(reg-event-fx
 :blockchain.account/refresh
 (interceptors-fx :spec false)
 (fn [{:keys [db]}]
   {:dispatch [:blockchain/load-my-addresses]}))

;;;
;;;
;;; update current address
;;;
;;;
(reg-event-db
 :current-address/update
 interceptors
 (fn [db [new-current-address]]
   (assoc db :current-address new-current-address)))
