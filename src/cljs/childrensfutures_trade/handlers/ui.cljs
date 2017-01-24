(ns childrensfutures-trade.handlers.ui
  (:require
   [cljs.spec :as s]

   [childrensfutures-trade.db :as db]

   [cljs-web3.core :as web3]

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
 :ui.new-goal/toggle-view
 (interceptors)
 (fn [db]
   (update db :show-new-goal? not)))

;;;
;;; TOGGLE NEW BID DIALOG
;;;
(reg-event-db
 :ui.new-bid/toggle-view
 (interceptors)
 (fn [db [goal-id]]
   (-> db
       (update :show-new-bid? not)
       (assoc-in [:new-bid :goal-id] goal-id))))

;;;
;;;
;;;
(reg-event-db
 :ui.drawer/toggle-view
 (interceptors)
 (fn [db]
   (update db :drawer-open? not)))

;;;
;;; toggle accounts view
;;;
(reg-event-db
 :ui.accounts/toggle-view
 (interceptors)
 (fn [db]
   (update db :show-accounts? not)))

(reg-event-fx
 :ui.set-current-page
 (interceptors)
 (fn [{:keys [db]} [match]]
   {:db (assoc db :current-page match
               :drawer-open? false)
    ;; :ga/page-view [(apply u/path-for (:handler match) (flatten (into [] (:route-params match))))]
    }))

;;;
;;;
;;; chat events
;;;
;;;
(reg-event-db
 :ui.chat/open
 (interceptors)
 (fn [db [channel-id]]
   (-> db
       (assoc :chat-open? true)
       (assoc :chat-channel-id (str "a" (subs (web3/sha3 channel-id) 2 30))))))
