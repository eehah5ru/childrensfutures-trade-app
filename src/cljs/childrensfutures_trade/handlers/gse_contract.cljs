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
  [[:GoalAdded :gse-contract/on-goal-loaded]
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
                 :uri (gstring/format "./contracts/build/%s.abi"
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
(reg-event-db
 :gse-contract/on-goal-loaded
 (interceptors)
 (fn [db [goal]]
   (assoc-in db
             [:goals (:goal-id goal)]
             (merge (db/default-goal)
                    (select-keys goal [:owner :description :goal-id :give-in-return])
                    {:stage :created}))))



;;;
;;;
;;; GoalCancelled contract event
;;;
;;;
(reg-event-db
 :gse-contract/on-goal-cancelled
 (interceptors)
 (fn [db [goal]]
   (js/console.log :debug :on-goal-cancelled (:goal-id goal))
   (-> db
       (assoc-in [:goals (:goal-id goal) :cancelled?] true)
       (assoc-in [:goals (:goal-id goal) :stage] :cancelled))))


;;;
;;;
;;; BidPlaced contract event
;;;
;;;
(reg-event-db
 :gse-contract/on-bid-placed
 (interceptors)
 (fn [db [bid]]
   (js/console.log :debug :bid-placed bid)
   (-> db
       (assoc-in [:goals (:goal-id bid) :stage] :bid-placed)
       (assoc-in [:goals (:goal-id bid) :bids (:bid-owner bid)] ; FIXME: bid-owner -> bid-id
                 (merge (db/default-bid)
                        (let [{:keys [bid-owner description goal-id]} bid]
                          {:goal-id goal-id
                           :bid-id bid-owner ;FIXME use bid-id instead
                           :owner bid-owner
                           :description description}))))))


;;;
;;;
;;; BidSelected
;;;
;;;
(reg-event-db
 :gse-contract/on-bid-selected
 (interceptors)

 (fn [db [{:keys [goal-id bid-id]}]]
   (js/console.log :info :bid-selected goal-id bid-id)
   (-> db
       (assoc-in [:goals goal-id :stage] :bid-selected)
       (assoc-in [:goals goal-id :bids bid-id :selected?]
                 true))))

;;;
;;;
;;; InvestmentSent
;;;
;;;
(reg-event-db
 :gse-contract/on-investment-sent
 (interceptors)
 (fn [db [{:keys [goal-id bid-id]}]]
   (js/console.log :info :investment-sent goal-id bid-id)
   (-> db
       (assoc-in [:goals goal-id :stage] :investment-sent))))


;;;
;;;
;;; InvestmentReceived
;;;
;;;
(reg-event-db
 :gse-contract/on-investment-received
 (interceptors)
 (fn [db [{:keys [goal-id bid-id]}]]
   (js/console.log :info :investment-received goal-id bid-id)
   (-> db
       (assoc-in [:goals goal-id :stage] :investment-received))))

;;;
;;;
;;; GoalAchieved
;;;
;;;
(reg-event-db
 :gse-contract/on-goal-achieved
 (interceptors)
 (fn [db [{:keys [goal-id]}]]
   (js/console.log :info :goal-achieved goal-id)
   (-> db
       (assoc-in [:goals goal-id :stage] :goal-achieved))))


;;;
;;;
;;; BonusAsked
;;;
;;;
(reg-event-db
 :gse-contract/on-bonus-asked
 (interceptors)
 (fn [db [{:keys [goal-id bid-id]}]]
   (js/console.log :info :bonus-asked goal-id bid-id)
   (-> db
       (assoc-in [:goals goal-id :stage] :bonus-asked))))

;;;
;;;
;;; BonusSent
;;;
;;;
(reg-event-db
 :gse-contract/on-bonus-sent
 (interceptors)
 (fn [db [{:keys [goal-id bid-id]}]]
   (js/console.log :info :bonus-sent goal-id bid-id)
   (-> db
       (assoc-in [:goals goal-id :stage] :bonus-sent))))


;;;
;;;
;;; GoalCompleted
;;;
;;;
(reg-event-db
 :gse-contract/on-goal-completed
 (interceptors)
 (fn [db [{:keys [goal-id]}]]
   (js/console.log :info :goal-completed goal-id)
   (-> db
       (assoc-in [:goals goal-id :stage] :goal-completed))))
