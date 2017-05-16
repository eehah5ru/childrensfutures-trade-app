(ns childrensfutures-trade.handlers.messages
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

   [childrensfutures-trade.handlers.interceptors :refer [interceptors
                                                         interceptors-fx]]

   [childrensfutures-trade.handlers.utils :as hu]
   ))

(reg-event-db
 :messages/set-channel-id
 (interceptors)
 (fn [db [& ids]]
   (when (empty? ids)
     (throw "channel id is empty!"))
   (let [channel-id (u/chat-channel-id ids)]
     (-> db
         (assoc :current-chat-channel-id channel-id)
         (assoc-in [:new-chat-message :channel-id] channel-id)))))

(reg-event-db
 :chat-thread/create
 (interceptors)

 (fn [db [user-id channel-id]]
   (let [chat-threads (get-in db [:chat-threads user-id] [])
         exists? (some #(= channel-id %) chat-threads)]
     (cond-> db
       (not exists?)
       (assoc-in [:chat-threads user-id]
                 (conj chat-threads channel-id))))))

(reg-event-fx
 :chat-threads/print-all
 (interceptors-fx :spec false)

 (fn [{:keys [db]}]
   (js/console.log :chat-threads (:chat-threads db))))
