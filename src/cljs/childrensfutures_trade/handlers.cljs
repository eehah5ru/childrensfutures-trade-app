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
   [childrensfutures-trade.utils :as u]))

;;;
;;;
;;; INTERCEPTORS
;;;
;;;

;;;
;;; spec
;;;
(defn check-and-throw
  "throw an exception if db doesn't match the spec"
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

;;;
;;; interceptor for usual event handlers
;;;
(def check-spec-interceptor (after (partial check-and-throw :childrensfutures-trade.db/db)))

;;;
;;; interceptor for FX event handlers
;;;
(def check-spec-interceptor-fx (after
                                (fn [db]
                                  (js/console.log :debug :interceptor-fx db)
                                  (check-and-throw :childrensfutures-trade.db/db db))))

;;;
;;; INTERCEPTORS DEFINITION
;;;
(def interceptors [check-spec-interceptor
                   #_(when ^boolean js/goog.DEBUG debug)
                   trim-v])

;;; interceptors for fx events
;; (def interceptors-fx [
;;                       check-spec-interceptor-fx
;;                       trim-v])

(defn interceptors-fx [{:keys [spec]} & rest]
  (let [default-interceptors [trim-v]]
    (if spec
      (conj default-interceptors check-spec-interceptor-fx)
      default-interceptors)))

(def goal-gas-limit 1000000)

;; (comment
;;   (dispatch [:contract/fetch-compiled-code [:contract/deploy-compiled-code]])
;;   (dispatch [:blockchain/unlock-account "0x6fce64667819c82a8bcbb78e294d7b444d2e1a29" "m"])
;;   (dispatch [:blockchain/unlock-account "0xc5aa141d3822c3368df69bfd93ef2b13d1c59aec" "m"])
;;   (dispatch [:blockchain/unlock-account "0xe206f52728e2c1e23de7d42d233f39ac2e748977" "m"])
;;   (dispatch [:blockchain/unlock-account "0x522f9c6b122f4ca8067eb5459c10d03a35798ed9" "m"])
;;   (dispatch [:blockchain/unlock-account "0x43100e355296c4fe3d2c0a356aa4151f1257393b" "m"])
;;   )




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
     :http-xhrio {:method :get
                  :uri (gstring/format "./contracts/build/%s.abi"
                                       (get-in db/default-db [:contract :name]))
                  :timeout 6000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:contract/abi-loaded]
                  :on-failure [:log-error]}
     :dispatch [:blockchain/load-my-addresses]})))


(reg-event-fx
 :blockchain/load-my-addresses
 (interceptors-fx :spec false)
 (fn [{:keys [db]}]
   (when (:provides-web3? db)
     {:web3-fx.blockchain/fns
      {:web3 (:web3 db)
       :fns [[web3-eth/accounts :blockchain/my-addresses-loaded :log-error]]}})))
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
    :web3-fx.blockchain/balances
    {:web3 (:web3 db/default-db)
     :addresses addresses
     :watch? true
     :blockchain-filter-opts "latest"
     :dispatches [:blockchain/balance-loaded :log-error]}}))


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


;;;
;;;
;;; GOAL
;;;
;;;

;;;
;;;
;;; UI RELATED
;;;
;;;

;;;
;;; toggle details visibility
;;;

;;;
;;; TOGGLE NEW GOAL VIEW VISIBILITY
;;;
(reg-event-db
 :new-goal/toggle-view
 interceptors
 (fn [db [goal-id]]
   (-> db
       (update :show-new-goal? not)
       (update-in [:new-bid :goal-id] goal-id))))

;;;
;;; TOGGLE NEW BID DIALOG
;;;
(reg-event-db
 :new-bid/toggle-view
 interceptors
 (fn [db]
   (update db :show-new-bid? not)))

;;;
;;;
;;;
(reg-event-db
 :drawer/toggle-view
 interceptors
 (fn [db]
   (update db :drawer-open? not)))

;;;
;;; toggle accounts view
;;;
(reg-event-db
 :accounts/toggle-view
 interceptors
 (fn [db]
   (update db :show-accounts? not)))

(reg-event-fx
 :set-current-page
 interceptors
 (fn [{:keys [db]} [match]]
   {:db (assoc db :current-page match
               :drawer-open? false)
    ;; :ga/page-view [(apply u/path-for (:handler match) (flatten (into [] (:route-params match))))]
    }))

;;;
;;;
;;; GOAL ACTIONS
;;;
;;;

;;;
;;;
;;; NEW GOAL
;;;
;;;


;;;
;;;
;;; update goal values while editing goal
;;;
;;;
(reg-event-db
 :new-goal/update
 interceptors
 (fn [db [key value]]
   (assoc-in db [:new-goal key] value)))

;;;
;;; send new goal to ethereum contract
;;;
(reg-event-fx
 :new-goal/send
 (interceptors-fx :spec false)
 (fn [{:keys [db]} []]
   (let [{:keys [description owner]} (:new-goal db)]
     {:web3-fx.contract/state-fn
      {:instance (:instance (:contract db))
       :web3 (:web3 db)
       :db-path [:contract :send-goal]
       :fn [:new-goal description
            {:from owner
             :gas goal-gas-limit}
            :new-goal/confirmed
            :log-error
            :new-goal/transaction-receipt-loaded]}})))


;;;
;;; change new-goal's state to sending
;;; fired after the trx has been confirmed
;;; see event handler above
;;;
(reg-event-db
 :new-goal/confirmed
 interceptors
 (fn [db [transaction-hash]]
   (-> db
       (assoc-in [:new-goal :sending?] true)
       (update :show-new-goal? not))))

;;;
;;; confirms that goal was sent to ethereum contract
;;;
(reg-event-db
 :new-goal/transaction-receipt-loaded
 interceptors
 (fn [db [{:keys [gas-used] :as transaction-receipt}]]
   (console :log transaction-receipt)
   (when (= gas-used goal-gas-limit)
     (console :error "All gas used"))
   (-> db
       (assoc :new-goal (db/default-goal))
       (assoc-in [:new-goal :owner] (:current-address db)))))


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
    :dispatch-n [[:new-bid/toggle-view]
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
