(ns childrensfutures-trade.handlers.chat-contract
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


   [childrensfutures-trade.handlers.utils :refer [goal-gas-limit]]

   ;;
   ;; event handlers
   ;;
   [childrensfutures-trade.handlers.interceptors :refer [interceptors
                                                         interceptors-fx]]

   [childrensfutures-trade.handlers.utils :as hu]
   ))

;;;
;;;
;;; CONSTANTS
;;;
;;;
(def history-step 10000)

;;;
;;;
;;; contract options
;;;
;;;

;;;
;;; from block options
;;;
;; (def from-block
;;   "latest")

(defn from-block [db]
  (get-in db [:chat-contract :from-block] 0))

;;;
;;;
;;; contract events
;;;
;;;


;;;
;;; ethereum events
;;;
(def contract-events
  [[:MessageSent :chat-contract/on-message-sent]])


;;;
;;; fetch chat's contract abi
;;;
(reg-event-fx
 :chat-contract/fetch-abi
 (interceptors-fx :spec false)
 (fn [{:keys [db]} _]
   (js/console.log :debug :chat-contract :abi-loading)
   {:http-xhrio {:method :get
                 :uri (gstring/format "/contracts/build/%s.abi"
                                      (get-in db/default-db [:chat-contract :name]))
                 :timeout 6000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:chat-contract/abi-loaded]
                 :on-failure [:log-error]}}))


;;;
;;; set chat contract options when abi has been loaded
;;;
(reg-event-fx
 :chat-contract/abi-loaded
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [abi]]
   (js/console.log :debug :chat-contract :abi-loaded)

   (let [web3 (:web3 db)
         contract-instance (web3-eth/contract-at web3 abi (-> db
                                                              :chat-contract
                                                              :address))
         from-block-n (from-block db)]
     {:db (assoc-in db [:chat-contract :instance] contract-instance)
      :dispatch [:chat-contract.latest-block/load]
      ; :web3-fx.contract/events
      #_{:instance contract-instance
       :db db
       :db-path [:chat-contract :events]
       :events (map (fn [[event handler]]
                      [contract-instance event {} {:from-block from-block-n} handler :log-error])
                    contract-events)}
      })))


;;;
;;; load latest block when app was just run
;;;
(reg-event-fx
 :chat-contract.latest-block/load
 (interceptors-fx :spec false)

 (fn [{:keys [db]}]
   {:web3-fx.blockchain/fns
    {:web3 (:web3 db)
     :fns [[web3-eth/get-block "latest"
              {}
              [:chat-contract.latest-block/loaded]
              :log-error]]}}))

;;;
;;; when latest block loaded
;;;
(reg-event-fx
 :chat-contract.latest-block/loaded
 (interceptors-fx :spec true)

 (fn [{:keys [db]} [latest-block]]
   (let [to-block (get latest-block :number 0)
         from-block (get-in db [:gse-contract :from-block] 0)]
     {:db (assoc-in db [:chat-contract :events-latest-block] to-block)
      :dispatch-n [[:chat-contract.block-loaded/setup-filter]
                   [:chat-contract.history/fetch from-block to-block]]})))


(defn- history-steps [from to]
  (for [x (range from to history-step)
        :let [y (+ x history-step)]]
    (if (>= y to)
      [x to]
      [x (dec y)])))

;;;
;;; fetch history of chat messages
;;;
(reg-event-fx
 :chat-contract.history/fetch
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [from-block to-block]]
   (let [contract-instance (get-in db [:chat-contract :instance])
         mk-events (fn [[event handler]]
                     (map (fn [[f-block t-block]]
                            (vector
                             contract-instance
                             (str event "-history-" f-block t-block)
                             event
                             {}
                             {:from-block f-block
                              :to-block t-block}
                             handler
                             :log-error))
                          (history-steps from-block to-block)))
         events (apply concat (map mk-events
                                   contract-events))]

     (js/console.log :chat.history/fetch from-block to-block)

     {:web3-fx.contract/events
      {:db db
       :db-path [:chat-contract :events :history]
       :events events}})))

;;;
;;; setup new block loaded filter
;;;
(reg-event-fx
 :chat-contract.block-loaded/setup-filter
 (interceptors-fx :spec false)

 (fn [{:keys [db]}]
   {:web3-fx.blockchain/filter
    {:web3 (:web3 db)
     :db-path [:gse-contract :block-loaded-filter]
     :blockchain-filter-opts "latest"
     :dispatches [:chat-contract.block-loaded/new :log-error]}}))

;;;
;;; handler for new loaded block
;;;
(reg-event-fx
 :gse-contract.block-loaded/new
 (interceptors-fx :spec false)
 (fn [{:keys [db]} [block-hash]]
   {:web3-fx.blockchain/fns
    {:web3 (:web3 db)
     :fns [[web3-eth/get-block block-hash
            {}
            :chat-contract.block-loaded/got-block-number
            :log-error]]}}))

;;;
;;; when got block number of new loaded block
;;;
(reg-event-fx
 :chat-contract.block-loaded/got-block-number
 (interceptors-fx :spec true)

 (fn [{:keys [db]} [block-data]]
   ;; (js/console.log :got-block-number block-data)
   (let [prev-events-latest-block (get-in db [:chat-contract :events-latest-block] 0)
         next-events-latest-block (get block-data :number 0)
         need-to-fetch-history? (> next-events-latest-block
                                   (+ 1 prev-events-latest-block))]
     (js/console.log :chat :got-block-number next-events-latest-block :resubscribing? need-to-fetch-history?)

     (if need-to-fetch-history?
       (do
         (js/console.log :chat :got-block-number :resubscribing (inc prev-events-latest-block) next-events-latest-block)

         {:db (assoc-in db [:chat-contract :events-latest-block] next-events-latest-block)
          :dispatch [:chat-contract.latest-events/subscribe (inc prev-events-latest-block) next-events-latest-block]})
       {}))))

;;;
;;; subscribe to events in few latest blocks
;;;
(reg-event-fx
 :chat-contract.latest-events/subscribe
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [from-block to-block]]
   (let [contract-instance (get-in db [:chat-contract :instance])]
     {:web3-fx.contract/events
      {:db db
       :db-path [:chat-contract :events :latest]
       :events (map (fn [[event handler]]
                      (vector contract-instance
                              (str event "-latest")
                              event
                              {}
                              {:from-block from-block
                               :to-block to-block}
                              handler
                              :log-error))
                    contract-events)}})))

;;;
;;;
;;; event for MessageSent contract event
;;;
;;;
(reg-event-fx
 :chat-contract/on-message-sent
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [msg]]
   {:db (let [{:keys [channel-id
                      msg-id
                      sender
                      message]} msg
              loaded-message (merge (db/default-chat-message)
                                    {:channel-id channel-id
                                     :message-id (.toNumber msg-id)
                                     :owner sender
                                     :text message})
              messages (get-in db [:messages channel-id] [])]
          (js/console.log :debug :received-msg loaded-message)
          (assoc-in db [:messages channel-id]
                    (conj messages loaded-message)))
    :dispatch-later [{:ms 200
                      :dispatch [:ui.chat/scroll-to-bottom]}]}))
