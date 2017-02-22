(ns childrensfutures-trade.handlers.gse-contract
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
;;; CONSTANTS
;;;
;;;
(def history-step 10000)

;;;
;;; from block options
;;;
(defn- from-block [db]
  (get-in db [:gse-contract :from-block] 0))

;;;
;;;
;;; contract events
;;;
;;;
(def contract-events
  [[:GoalAdded :gse-contract/on-goal-added]
   [:BidPlaced :gse-contract/on-bid-placed]
   [:BidSelected :gse-contract/on-bid-selected]
   [:InvestmentSent :gse-contract/on-investment-sent]
   [:InvestmentReceived :gse-contract/on-investment-received]
   [:GoalAchieved :gse-contract/on-goal-achieved]
   [:BonusAsked :gse-contract/on-bonus-asked]
   [:BonusSent :gse-contract/on-bonus-sent]
   [:GoalCompleted :gse-contract/on-goal-completed]
   [:GoalCancelled :gse-contract/on-goal-cancelled]
   ])


;;;
;;;
;;; fetch contract abi
;;;
;;;
(reg-event-fx
 :gse-contract/fetch-abi
 (interceptors-fx :spec false)
 (fn [{:keys [db]} [fetched-db-version]]
   (js/console.log :debug :abi-loading)
   {:http-xhrio {:method :get
                 :uri (gstring/format "/contracts/build/%s.abi"
                                      (get-in db/default-db [:gse-contract :name]))
                 :timeout 6000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:gse-contract/abi-loaded fetched-db-version]
                 :on-failure [:log-error]}}))

;;;
;;;
;;; register handlers for contract events
;;;
;;;
(reg-event-fx
 :gse-contract/abi-loaded
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [fetched-db-version abi]]
   (js/console.log :debug :abi-loaded)

   (let [web3 (:web3 db)
         contract-instance (web3-eth/contract-at web3 abi (-> db
                                                              :gse-contract
                                                              :address))]
     ;; (js/console.log :latest-block (web3-eth/block-number web3 "latest"))
     {:db (assoc-in db [:gse-contract :instance] contract-instance)

      ;; contract calls
      :web3-fx.contract/constant-fns
      {:instance contract-instance
       :fns [[contract-instance
              :is-working
              :gse-contract/is-working-loaded :log-error]]}

      :dispatch-n [[:gse-contract.history/init fetched-db-version]
                   [:gse-contract/subscribe-to-events]]})))

;;;
;;; init blockchain logs
;;;
(reg-event-fx
 :gse-contract.history/init
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [fetched-db-version]]
   (let [web3 (:web3 db)]
     {:web3-fx.blockchain/fns
      {:web3 web3
       :fns [[web3-eth/get-block "latest"
              {}
              [:gse-contract.history/latest-block-loaded fetched-db-version]
              :log-error]]}})))

;;;
;;; when latest blockhain block loaded
;;;
(reg-event-fx
 :gse-contract.history/latest-block-loaded
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [fetched-db-version latest-block]]
   ;; (js/console.log :latest-block latest-block)
   (let [to-block (get latest-block :number 0)
         from-block (inc fetched-db-version)
         need-to-fetch-history? (> to-block fetched-db-version)]

     (if need-to-fetch-history?
       {:dispatch-n (map (fn [[event handler]]
                           (vector :gse-contract.history/fetch-history
                                   event
                                   handler
                                   from-block
                                   to-block))
                         contract-events)}
       {}))))

;;;
;;;
;;; fetch history from block to block for specific event
;;;
;;;

;;;
;;; utils
;;;
(defn- history-steps [from to]
  (for [x (range from to history-step)
        :let [y (+ x history-step)]]
    (if (>= y to)
      [x to]
      [x (dec y)])))
;;;
;;; fetch history event
;;;
(reg-event-fx
 :gse-contract.history/fetch-history
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [event handler from-block to-block]]
   (let [contract-instance (:instance (:gse-contract db))
         contract-start-block (get-in db [:gse-contract :from-block] 0)
         from-block (max contract-start-block from-block)]
     (js/console.log :gse/fetch-history event from-block to-block)

     {:web3-fx.contract/events
      {:db db
       :db-path [:gse-contract :events]
       :events (map (fn [[from-block to-block]]
                      (vector contract-instance
                              (str event "-history-" from-block to-block)
                              event
                              {}
                              {:from-block from-block
                               :to-block to-block}
                              handler
                              :log-error))
                    (history-steps from-block to-block))}})))


;;;
;;;
;;; setup events
;;;
;;;
(reg-event-fx
 :gse-contract/subscribe-to-events
 (interceptors-fx :spec false)

 (fn [{:keys [db]}]
   (let [from-block-n "latest"
         contract-instance (:instance (:gse-contract db))]
     {:web3-fx.contract/events
      {:db db
       :db-path [:gse-contract :events]
       :events (map (fn [[event handler]]
                      [contract-instance
                       event
                       {}
                       {:from-block from-block-n}
                       handler
                       :log-error])
                    contract-events)}})))

;;;
;;;
;;; GSE FUNCTION CALLS
;;;
;;;
(reg-event-fx
 :gse-contract/is-working-loaded
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [is-working?]]
   (js/console.log :debug :is-working-loaded is-working?)
   (let [no-contract? (not is-working?)]
     (if no-contract?
       {:dispatch [:app/critical-error]}
       {}))))

;;;
;;;
;;; GSE CONTRACT EVENTS
;;;
;;;

;;;
;;;
;;; event for GoalAdded contract event
;;;
;;;
(reg-event-fx
 :gse-contract/on-goal-added
 (interceptors-fx :spec false)
 (fn [{:keys [db]} [goal {:keys [block-number]}]]
   (js/console.log :debug :on-goal-added (:goal-id goal))
   {:dispatch-n [[:db.goal/add goal block-number]
                 [:pulse/push-goal-added block-number (:goal-id goal)]
                 [:sync-db/on-db-updated block-number]]
    }))



;;;
;;;
;;; GoalCancelled contract event
;;;
;;;
(reg-event-fx
 :gse-contract/on-goal-cancelled
 (interceptors-fx :spec false)
 (fn [{:keys [db]} [goal {:keys [block-number]}]]
   (js/console.log :debug :on-goal-cancelled (:goal-id goal))
   {:dispatch-n [[:db.goal/cancel goal]
                 [:pulse/push-goal-cancelled block-number (:goal-id goal)]
                 [:sync-db/on-db-updated block-number]]}))


;;;
;;;
;;; BidPlaced contract event
;;;
;;;
(reg-event-fx
 :gse-contract/on-bid-placed
 (interceptors-fx :spec false)
 (fn [{:keys [db]} [bid {:keys [block-number]}]]
   (js/console.log :debug :bid-placed bid)
   {:dispatch-n [[:db.goal/place-bid bid]
                 [:pulse/push-investment-placed block-number (:goal-id bid) (:bid-owner bid)]
                 [:sync-db/on-db-updated block-number]]}))


;;;
;;;
;;; BidSelected
;;;
;;;
(reg-event-fx
 :gse-contract/on-bid-selected
 (interceptors-fx :spec false)

 (fn [_ [{:keys [goal-id bid-id goal-owner bid-owner]} {:keys [block-number]}]]
   (js/console.log :info :bid-selected goal-id bid-id)

   {:dispatch-n [[:db.goal/select-bid goal-id bid-id]
                 [:chat-thread/create goal-owner (u/chat-channel-id goal-id bid-id)]
                 [:chat-thread/create bid-owner (u/chat-channel-id goal-id bid-id)]
                 [:pulse/push-investment-selected block-number goal-id bid-id]
                 [:sync-db/on-db-updated block-number]]}))

;;;
;;;
;;; InvestmentSent
;;;
;;;
(reg-event-fx
 :gse-contract/on-investment-sent
 (interceptors-fx :spec false)

 (fn [db [{:keys [goal-id bid-id]} {:keys [block-number]}]]
   (js/console.log :info :investment-sent goal-id bid-id)

   {:dispatch-n [[:db.goal/send-investment goal-id bid-id]
                 [:pulse/push-investment-sent block-number goal-id bid-id]
                 [:sync-db/on-db-updated block-number]]}))


;;;
;;;
;;; InvestmentReceived
;;;
;;;
(reg-event-fx
 :gse-contract/on-investment-received
 (interceptors-fx :spec false)

 (fn [db [{:keys [goal-id bid-id]} {:keys [block-number]}]]
   (js/console.log :info :investment-received goal-id bid-id)

   {:dispatch-n [[:db.goal/receive-investment goal-id bid-id]
                 [:pulse/push-investment-received block-number goal-id bid-id]
                 [:sync-db/on-db-updated block-number]]}))

;;;
;;;
;;; GoalAchieved
;;;
;;;
(reg-event-fx
 :gse-contract/on-goal-achieved
 (interceptors-fx :spec false)

 (fn [db [{:keys [goal-id]} {:keys [block-number]}]]
   (js/console.log :info :goal-achieved goal-id)

   {:dispatch-n [[:db.goal/achieve goal-id]
                 [:pulse/push-goal-achieved block-number goal-id]
                 [:sync-db/on-db-updated block-number]]}))


;;;
;;;
;;; BonusAsked
;;;
;;;
(reg-event-fx
 :gse-contract/on-bonus-asked
 (interceptors-fx :spec false)
 (fn [db [{:keys [goal-id bid-id]} {:keys [block-number]}]]
   (js/console.log :info :bonus-asked goal-id bid-id)

   {:dispatch-n [[:db.goal/ask-bonus goal-id bid-id]
                 [:pulse/push-bonus-asked block-number goal-id bid-id]
                 [:sync-db/on-db-updated block-number]]}))

;;;
;;;
;;; BonusSent
;;;
;;;
(reg-event-fx
 :gse-contract/on-bonus-sent
 (interceptors-fx :spec false)

 (fn [db [{:keys [goal-id bid-id]} {:keys [block-number]}]]
   (js/console.log :info :bonus-sent goal-id bid-id)

   {:dispatch-n [[:db.goal/send-bonus goal-id bid-id]
                 [:pulse/push-bonus-sent block-number goal-id bid-id]
                 [:sync-db/on-db-updated block-number]]}))


;;;
;;;
;;; GoalCompleted
;;;
;;;
(reg-event-fx
 :gse-contract/on-goal-completed
 (interceptors-fx :spec false)

 (fn [db [{:keys [goal-id]} {:keys [block-number]}]]
   (js/console.log :info :goal-completed goal-id)

   {:dispatch-n [[:db.goal/complete goal-id]
                 [:pulse/push-goal-completed block-number goal-id]
                 [:sync-db/on-db-updated block-number]]}))
