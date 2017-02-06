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
;;; from block options
;;;
;; (def from-block
;;   "latest")

(def from-block
  {:from-block 0})

;;;
;;;
;;; contract events
;;;
;;;
(def contract-events
  [[:GoalAdded :gse-contract/on-goal-added]
   [:GoalCancelled :gse-contract/on-goal-cancelled]
   [:BidPlaced :gse-contract/on-bid-placed]
   [:BidSelected :gse-contract/on-bid-selected]
   [:InvestmentSent :gse-contract/on-investment-sent]
   [:InvestmentReceived :gse-contract/on-investment-received]
   [:GoalAchieved :gse-contract/on-goal-achieved]
   [:BonusAsked :gse-contract/on-bonus-asked]
   [:BonusSent :gse-contract/on-bonus-sent]
   [:GoalCompleted :gse-contract/on-goal-completed]])


;;;
;;;
;;; fetch contract abi
;;;
;;;
(reg-event-fx
 :gse-contract/fetch-abi
 (interceptors-fx :spec false)
 (fn [{:keys [db]} _]
   {:http-xhrio {:method :get
                 :uri (gstring/format "/contracts/build/%s.abi"
                                      (get-in db/default-db [:gse-contract :name]))
                 :timeout 6000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:gse-contract/abi-loaded]
                 :on-failure [:log-error]}}))

;;;
;;;
;;; register handlers for contract events
;;;
;;;
(reg-event-fx
 :gse-contract/abi-loaded
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [abi]]
   (let [web3 (:web3 db)
         contract-instance (web3-eth/contract-at web3 abi (-> db
                                                              :gse-contract
                                                              :address))]
     {:db (assoc-in db [:gse-contract :instance] contract-instance)

      :web3-fx.contract/events
      {:instance contract-instance
       :db db
       :db-path [:gse-contract :events]
       :events (map (fn [[event handler]]
                      [event {} from-block handler :log-error])
                    contract-events)}
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
(reg-event-fx
 :gse-contract/on-goal-added
 (interceptors-fx :spec false)
 (fn [{:keys [db]} [goal {:keys [block-number]}]]
   (js/console.log :debug :on-goal-added (:goal-id goal))
   {:dispatch-n [[:db.goal/add goal block-number]
                 [:pulse/push-goal-added block-number (:goal-id goal)]
                 [:sync-db/inc-db-version block-number]]
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
                 [:sync-db/inc-db-version block-number]]}))


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
                 [:sync-db/inc-db-version block-number]]}))


;;;
;;;
;;; BidSelected
;;;
;;;
(reg-event-fx
 :gse-contract/on-bid-selected
 (interceptors-fx :spec false)

 (fn [_ [{:keys [goal-id bid-id]} {:keys [block-number]}]]
   (js/console.log :info :bid-selected goal-id bid-id)

   {:dispatch-n [[:db.goal/select-bid goal-id bid-id]
                 [:sync-db/inc-db-version block-number]]}))

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
                 [:sync-db/inc-db-version block-number]]}))


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
                 [:sync-db/inc-db-version block-number]]}))

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
                 [:sync-db/inc-db-version block-number]]}))


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
                 [:sync-db/inc-db-version block-number]]}))

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
                 [:sync-db/inc-db-version block-number]]}))


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
                 [:sync-db/inc-db-version block-number]]}))
