(ns childrensfutures-trade.handlers
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
   [childrensfutures-trade.handlers.blockchain]
   [childrensfutures-trade.handlers.contract]
   [childrensfutures-trade.handlers.new-goal]
   [childrensfutures-trade.handlers.ui]))



;;;
;;;
;;; initial event
;;;
;;;
(reg-event-fx
 :initialize
 (fn [_ _]
   (merge
    {:db db/default-db
     ;; TODO: refactor and extract this code to contract/fetch-abi
     :http-xhrio {:method :get
                  :uri (gstring/format "./contracts/build/%s.abi"
                                       (get-in db/default-db [:contract :name]))
                  :timeout 6000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:contract/abi-loaded]
                  :on-failure [:log-error]}
     :dispatch [:blockchain/load-my-addresses]})))



;;;
;;;
;;; GOAL
;;;
;;;


;;;
;;;
;;; GOAL ACTIONS
;;;
;;;


;;;
;;;
;;; CANCEL GOAL
;;;
;;;

;;;
;;; make cancel goal trx in the ethereum contract
;;;
(reg-event-fx
 :cancel-goal/send
 (interceptors-fx :spec false)
 (fn [{:keys [db]} [goal-id]]
   (let [address (:current-address db)]
     {:web3-fx.contract/state-fn
      {:instance (:instance (:contract db))
       :web3 (:web3 db)
       :db-path [:contract :cancel-goal (keyword goal-id)]
       :fn [:cancel-goal goal-id
            {:from address
             :gas goal-gas-limit}
            [:cancel-goal/confirmed goal-id]
            :log-error
            [:cancel-goal/transaction-receipt-loaded goal-id]]}})))

;;;
;;; change state of cancelled goal
;;; if trx was confirmed by user
;;;
(reg-event-db
 :cancel-goal/confirmed
 interceptors
 (fn [db [goal-id tx-hash]]
   (assoc-in db [:goals goal-id :cancelling?] true)))

;;;
;;; confirms that goal was cancelled
;;;
(reg-event-db
 :cancel-goal/transaction-receipt-loaded
 interceptors
 (fn [db [goal-id & {:keys [gas-used] :as transaction-receipt}]]
   (console :log transaction-receipt)
   (when (= gas-used goal-gas-limit)
     (console :error "All gas used"))
   (-> db
       (assoc-in [:goals goal-id :cancelling?] false))))


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
                 [:place-bid/send goal-id]]}))

;;;
;;; make place bid trx in the ethereum
;;;
(reg-event-fx
 :place-bid/send
 (interceptors-fx :spec false)
 (fn [{:keys [db]} [goal-id]]
   (let [address (:current-address db)
         {:keys [description]} (:new-bid db) ]
     {:web3-fx.contract/state-fn
      {:instance (:instance (:contract db))
       :web3 (:web3 db)
       :db-path [:contract :place-bid (keyword goal-id)]
       :fn [:place-bid goal-id description
            {:from address
             :gas goal-gas-limit}
            [:place-bid/confirmed goal-id]
            :log-error
            [:place-bid/transaction-receipt-loaded goal-id]]}})))

;;;
;;; change state of placed bid
;;; if trx was confirmed by user
;;;
(reg-event-db
 :place-bid/confirmed
 interceptors
 (fn [db [goal-id tx-hash]]
   (assoc-in db [:new-bid :placing?] true)))

;;;
;;; confirms that bid was placed
;;;
(reg-event-db
 :place-bid/transaction-receipt-loaded
 interceptors
 (fn [db [goal-id & {:keys [gas-used] :as transaction-receipt}]]
   (console :log transaction-receipt)
   (when (= gas-used goal-gas-limit)
     (console :error "All gas used"))
   (-> db
       (assoc-in [:new-bid :placing?] false))))

;;;
;;; show new bid form
;;;
(reg-event-db
 :place-bid/show-new-bid
 interceptors
 (fn [db [goal-id]]
   (-> db
       (assoc :show-new-bid? true)
       (assoc-in [:new-bid :goal-id] goal-id))))


;;;
;;; update new bid values while editing bid
;;;
(reg-event-db
 :place-bid/update
 interceptors
 (fn [db [goal-id key value]]
   (assoc-in db [:new-bid key] value)))


;;;
;;; cancel new bid
;;;
(reg-event-db
 :place-bid/cancel
 interceptors
 (fn [db [goal-id]]
   (-> db
       (assoc :show-new-bid? false)
       (assoc :new-bid (db/default-bid)))))


;;;
;;;
;;; SELECT BID
;;;
;;;

;;;
;;; make select bid trx in the ethereum
;;;
(reg-event-fx
 :select-bid/send
 (interceptors-fx :spec false)
 (fn [{:keys [db]} [goal-id bid-id]]
   (let [address (:current-address db)]
     {:web3-fx.contract/state-fn
      {:instance (:instance (:contract db))
       :web3 (:web3 db)
       :db-path [:contract :select-bid (keyword goal-id) (keyword bid-id)]
       :fn [:select-bid goal-id bid-id
            {:from address
             :gas goal-gas-limit}
            [:select-bid/confirmed goal-id bid-id]
            :log-error
            [:select-bid/transaction-receipt-loaded goal-id bid-id]]}})))

;;;
;;; change state of selected bid
;;; if trx was confirmed by user
;;;
(reg-event-db
 :select-bid/confirmed
 interceptors
 (fn [db [goal-id bid-id tx-hash]]
   (assoc-in db [:goals goal-id :bids bid-id :selecting?] true)))

;;;
;;; confirms that bid was selected
;;;
(reg-event-db
 :select-bid/transaction-receipt-loaded
 interceptors
 (fn [db [goal-id bid-id & {:keys [gas-used] :as transaction-receipt}]]
   (console :log transaction-receipt)
   (when (= gas-used goal-gas-limit)
     (console :error "All gas used"))
   (-> db
       (assoc-in [:goals goal-id :bids bid-id :selecting?] false))))


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

;;;
;;;
;;; OLD STUFF
;;;
;;;

;; (reg-event-db
;;   :contract/settings-loaded
;;   interceptors
;;   (fn [db [[max-name-length max-tweet-length]]]
;;     (assoc db :settings {:max-name-length (.toNumber max-name-length)
;;                          :max-tweet-length (.toNumber max-tweet-length)})))


;; (reg-event-fx
;;   :contract/fetch-compiled-code
;;   interceptors
;;   (fn [{:keys [db]} [on-success]]
;;     {:http-xhrio {:method :get
;;                   :uri (gstring/format "/contracts/build/%s.json"
;;                                        (get-in db [:contract :name]))
;;                   :timeout 6000
;;                   :response-format (ajax/json-response-format {:keywords? true})
;;                   :on-success on-success
;;                   :on-failure [:log-error]}}))

;; (reg-event-fx
;;   :contract/deploy-compiled-code
;;   interceptors
;;   (fn [{:keys [db]} [contracts]]
;;     (let [{:keys [abi bin]} (get-in contracts [:contracts (keyword (:name (:contract db)))])]
;;       {:web3-fx.blockchain/fns
;;        {:web3 (:web3 db)
;;         :fns [[web3-eth/contract-new
;;                (js/JSON.parse abi)
;;                {:gas 4500000
;;                 :data bin
;;                 :from (first (:my-addresses db))}
;;                :contract/deployed
;;                :log-error]]}})))

;; (reg-event-fx
;;   :blockchain/unlock-account
;;   interceptors
;;   (fn [{:keys [db]} [address password]]
;;     {:web3-fx.blockchain/fns
;;      {:web3 (:web3 db)
;;       :fns [[web3-personal/unlock-account address password 999999
;;              :blockchain/account-unlocked
;;              :log-error]]}}))

;; (reg-event-fx
;;   :blockchain/account-unlocked
;;   interceptors
;;   (fn [{:keys [db]}]
;;     (console :log "Account was unlocked.")
;;     {}))

;; (reg-event-fx
;;   :contract/deployed
;;   interceptors
;;   (fn [_ [contract-instance]]
;;     (when-let [address (aget contract-instance "address")]
;;       (console :log "Contract deployed at" address))))
