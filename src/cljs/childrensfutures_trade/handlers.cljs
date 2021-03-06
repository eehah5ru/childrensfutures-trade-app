(ns childrensfutures-trade.handlers
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
   [childrensfutures-trade.handlers.blockchain]
   [childrensfutures-trade.handlers.gse-contract]
   ;; chat blockchain events
   [childrensfutures-trade.handlers.chat-contract]

   ;; blockchain GSE contract events
   [childrensfutures-trade.handlers.goal] ; general goal events
   [childrensfutures-trade.handlers.new-goal]
   [childrensfutures-trade.handlers.place-bid]
   [childrensfutures-trade.handlers.select-bid]
   [childrensfutures-trade.handlers.send-investment]
   [childrensfutures-trade.handlers.receive-investment]
   [childrensfutures-trade.handlers.achieve-goal]
   [childrensfutures-trade.handlers.ask-bonus]
   [childrensfutures-trade.handlers.send-bonus]
   [childrensfutures-trade.handlers.complete-goal]

   [childrensfutures-trade.handlers.new-chat-message]
   [childrensfutures-trade.handlers.messages]
   [childrensfutures-trade.handlers.pulse]
   [childrensfutures-trade.handlers.sync-db]


   [childrensfutures-trade.handlers.ui]))



;;;
;;;
;;; initial event
;;;
;;;
(reg-event-fx
 :initialize
 (interceptors-fx :spec true)

 (fn [_ _]
   (js/console.log :initialize)
   (let [full-app? (db/provides-web3?)
         read-only-app? (not full-app?)
         db (cond-> db/default-db
              full-app?
              (assoc :web3 (db/mk-web3))

              full-app?
              (assoc :provides-web3? full-app?))]
     (js/console.log :initialize :full-app? full-app?)
     ;; (js/console.log :initialize :db db)

     {:db db

      ;; TODO: refactor and extract this code to contract/fetch-abi
      :dispatch-n (cond-> []
                    ;; hide spinner imidiatelly for read-only-app
                    read-only-app?
                    (conj [:ui.spinner/hide])

                    full-app?
                    (conj [:ui.spinner/hide-later 3000])

                    ;; full-app?
                    ;; (conj [:blockchain/init])
                    ;; full-app?
                    ;; (conj [:chat-contract/fetch-abi])

                    ;; full-app?
                    ;; (conj [:gse-contract/fetch-abi])

                    full-app?
                    (conj [:initialize.full/fetch-db])

                    read-only-app?
                    (conj [:sync-db/fetch-forever])

                    true
                    (conj [:ui.window/set-size])

                    full-app?
                    (conj [:blockchain/load-my-addresses]))})))


(reg-event-fx
 :initialize.full/fetch-db
 (interceptors-fx :spec false)

 (fn [_]
   {:dispatch [:sync-db/fetch
               {:dispatch-n [[:chat-contract/fetch-abi]
                             [:gse-contract/fetch-abi]]}]}))



;;;
;;; make cancel goal trx in the ethereum contract
;;;
(reg-event-fx
 :cancel-goal/send
 (interceptors-fx :spec false)
 (fn [{:keys [db]} [goal-id]]
   (let [address (:current-address db)
         contract-instance (:instance (:gse-contract db))]
     {:web3-fx.contract/state-fns
      {:instance contract-instance
       :web3 (:web3 db)
       :db-path [:gse-contract :cancel-goal (keyword goal-id)]
       :fns [[contract-instance
              :cancel-goal goal-id
              {:from address
               :gas goal-gas-limit}
              [:cancel-goal/confirmed goal-id]
              :log-error
              [:cancel-goal/transaction-receipt-loaded goal-id]]]}})))

;;;
;;; change state of cancelled goal
;;; if trx was confirmed by user
;;;
(reg-event-db
 :cancel-goal/confirmed
 (interceptors)
 (fn [db [goal-id tx-hash]]
   (assoc-in db [:goals goal-id :trx-on-air?] true)))

;;;
;;; confirms that goal was cancelled
;;;
(reg-event-db
 :cancel-goal/transaction-receipt-loaded
 (interceptors)
 (fn [db [goal-id & {:keys [gas-used] :as transaction-receipt}]]
   (console :log transaction-receipt)
   (when (= gas-used goal-gas-limit)
     (console :error "All gas used"))
   (-> db
       (assoc-in [:goals goal-id :trx-on-air?] false))))


;;;
;;;
;;; PLACE BID ON GOAL
;;;
;;;

;;;
;;; handle place-button
;;;
(reg-event-fx
 :place-bid/place
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [goal-id]]
   {:db db
    :dispatch-n [[:ui.new-bid/toggle-view goal-id]
                 [:blockchain.place-bid/send goal-id]]}))

;;;
;;; make place bid trx in the ethereum
;;;
;; (reg-event-fx
;;  :place-bid/send
;;  (interceptors-fx :spec false)
;;  (fn [{:keys [db]} [goal-id]]
;;    (let [address (:current-address db)
;;          {:keys [description]} (:new-bid db)
;;          contract-instance (:instance (:gse-contract db))]
;;      {:web3-fx.contract/state-fns
;;       {:instance contract-instance
;;        :web3 (:web3 db)
;;        :db-path [:gse-contract :place-bid (keyword goal-id)]
;;        :fns [[contract-instance
;;               :place-bid goal-id description
;;               {:from address
;;                :gas goal-gas-limit}
;;               [:place-bid/confirmed goal-id]
;;               :log-error
;;               [:place-bid/transaction-receipt-loaded goal-id]]]}})))

;; ;;;
;; ;;; change state of placed bid
;; ;;; if trx was confirmed by user
;; ;;;
;; (reg-event-db
;;  :place-bid/confirmed
;;  (interceptors)
;;  (fn [db [goal-id tx-hash]]
;;    (assoc-in db [:new-bid :placing?] true)))

;; ;;;
;; ;;; confirms that bid was placed
;; ;;;
;; (reg-event-db
;;  :place-bid/transaction-receipt-loaded
;;  (interceptors)
;;  (fn [db [goal-id & {:keys [gas-used] :as transaction-receipt}]]
;;    (console :log transaction-receipt)
;;    (when (= gas-used goal-gas-limit)
;;      (console :error "All gas used"))
;;    (-> db
;;        (assoc-in [:new-bid :placing?] false))))

;;;
;;; show new bid form
;;;
(reg-event-db
 :place-bid/show-new-bid
 (interceptors)
 (fn [db [goal-id]]
   (-> db
       (assoc :show-new-bid? true)
       (assoc-in [:new-bid :goal-id] goal-id))))


;;;
;;; update new bid values while editing bid
;;;
(reg-event-db
 :place-bid/update
 (interceptors)
 (fn [db [goal-id key value]]
   (assoc-in db [:new-bid key] value)))


;;;
;;; cancel new bid
;;;
(reg-event-db
 :place-bid/cancel
 (interceptors)
 (fn [db [goal-id]]
   (-> db
       (assoc :show-new-bid? false)
       (assoc :new-bid (db/default-bid)))))



;;;
;;;
;;; LOGGER
;;;
;;;
(reg-event-fx
 :log-error
 (interceptors-fx :spec false)
 (fn [_ [err]]
   (js/console.log :error err)
   {}))

(reg-event-db
 :debug/print-db
 (interceptors)

 (fn [db]
   (js/console.log :debug/print-db db)
   db))
