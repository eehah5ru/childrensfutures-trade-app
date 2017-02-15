(ns childrensfutures-trade.handlers.chat-contract
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
   (let [web3 (:web3 db)
         contract-instance (web3-eth/contract-at web3 abi (-> db
                                                              :chat-contract
                                                              :address))
         from-block-n (from-block db)]
     {:db (assoc-in db [:chat-contract :instance] contract-instance)

      :web3-fx.contract/events
      {:instance contract-instance
       :db db
       :db-path [:chat-contract :events]
       :events (map (fn [[event handler]]
                      [event {} {:from-block from-block-n} handler :log-error])
                    contract-events)}
      })))


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
