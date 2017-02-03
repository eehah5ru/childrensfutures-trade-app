(ns childrensfutures-trade.handlers.ui
  (:require
   [cljs.spec :as s]

   [childrensfutures-trade.db :as db]

   [goog.string :as gstring]
   [goog.string.format]
   [madvas.re-frame.web3-fx]
   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console dispatch]]
   [childrensfutures-trade.utils :as u]

   [childrensfutures-trade.pages :as pages]
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

;;;
;;;
;;; chat events
;;;
;;;

;;;
;;; toggle chat view
;;;
(reg-event-db
 :ui.chat/toggle-view
 (interceptors)
 (fn [db]
   (update db :chat-open? not)))

;;;
;;; scroll chat messages to bottom
;;;
(reg-event-fx
 :ui.chat/scroll-to-bottom
 (interceptors-fx :spec false)
 (fn [{:keys [db]}]
   (u/scroll-to-bottom "chat-messages-container" 200)))

;;;
;;; set current page
;;;
(reg-event-fx
 :ui.set-current-page
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [match]]
   {:db (assoc db
               :current-page match
               :drawer-open? false)
    ;; :ga/page-view [(apply u/path-for (:handler match) (flatten (into [] (:route-params match))))]
    :dispatch [:ui.page.title/update]}))

;;;
;;; forse set window size
;;;
(reg-event-db
 :ui.window/set-size
 (interceptors)
 (fn [db]
   (assoc db :window-height (.-innerHeight js/window))))

;;;
;;; resized window
;;;
(reg-event-db
 :ui.window/resize
 (interceptors)
 (fn [db]
   ;; (js/console.log :debug :win-height (.-innerHeight js/window))
   (assoc db :window-height (.-innerHeight js/window))))

;;;
;;; change page title
;;;
(reg-event-fx
 :ui.page.title/update
 (interceptors-fx :spec false)
 (fn [{:keys [db]}]
   (let [cur-page-name (-> db
                      :current-page
                      :handler
                      pages/human-readable)
         page-title (str "myfutures.trade / " cur-page-name)]
     (aset js/document "title" page-title)
     {})))
