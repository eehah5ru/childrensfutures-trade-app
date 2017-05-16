(ns childrensfutures-trade.handlers.utils
  (:require
   [cljs.spec :as s]

   [goog.string :as gstring]
   [goog.string.format]
   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console dispatch]]

   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs-web3.personal :as web3-personal]
   ;; [cljsjs.web3]

   [childrensfutures-trade.db :as db]

   ))

(def goal-gas-limit 1500000)


;;;
;;; send transaction to blockchain
;;;
(defn blockchain-send-transaction [db
                                   contract
                                   f-name
                                   f-args & {:keys [db-path
                                                    confirmed-event
                                                    error-event
                                                    receipt-loaded-event]}]
  (let [address (:current-address db)
        fn-options {:from address
                    :gas goal-gas-limit}
        error-event (or error-event :log-error)
        contract-instance (:instance (contract db))]
    {:web3-fx.contract/state-fns
     {:instance contract-instance
      :web3 (:web3 db)
      :db-path (flatten [contract
                         f-name
                         (map keyword db-path)])
      :fns [(concat [contract-instance]
                    (flatten [f-name f-args])
                    [fn-options
                     confirmed-event
                     error-event
                     receipt-loaded-event])]}}))


;;;
;;; on trx receipt loaded
;;; f - db modifier. args are: [db [event-args]]
;;;
;; (defn blockchain-trx-receipt-loaded [f]
;;   (fn [db args]
;;     (js/console.log :debug :blockchain-trx-receipt-loaded all)
;;     db))

(defn blockchain-trx-receipt-loaded [f]
  (fn [db args]
    (let [[_ {:keys [gas-used] :as transaction-receipt}] args]
        (console :log :trx-receipt-loaded transaction-receipt)
     (when (= gas-used goal-gas-limit)
       (console :error "All gas used")))
    (f db args)))
