(ns childrensfutures-trade.handlers.ui
  (:require
   [cljs.spec :as s]
   [bidi.bidi :as bidi]

   [childrensfutures-trade.db :as db]

   [goog.string :as gstring]
   [goog.string.format]
   [madvas.re-frame.web3-fx]
   [madvas.re-frame.google-analytics-fx]

   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console dispatch]]
   [childrensfutures-trade.utils :as u]

   [childrensfutures-trade.pages :as pages :refer [path-for]]
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
;;; show/hide shae goal url
;;;
(reg-event-db
 :ui.goal/toggle-share-url
 (interceptors)
 (fn [db [goal-id]]
   (update-in db [:goals goal-id :show-share-url?] not)))

(reg-event-db
 :ui.goal/show-share-url
 (interceptors)
 (fn [db [goal-id]]
   (assoc-in db [:goals goal-id :show-share-url?] true)))

(reg-event-db
 :ui.goal/hide-share-url
 (interceptors)
 (fn [db [goal-id]]
   (assoc-in db [:goals goal-id :show-share-url?] false)))

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
;;; SELECT BID DIALOG EVENTS
;;;
(reg-event-db
 :ui.select-bid-dialog/toggle-view
 (interceptors)
 (fn [db]
   (update-in db [:select-bid :dialog-open?] not)))

(reg-event-db
 :ui.select-bid-dialog/set-selected
 (interceptors)
 (fn [db [goal-id bid-id]]
   (-> db
       (assoc-in [:select-bid :goal-id] goal-id)
       (assoc-in [:select-bid :bid-id] bid-id))))

(reg-event-db
 :ui.select-bid-dialog/clear-selected
 (interceptors)
 (fn [db]
   (-> db
       (assoc-in [:select-bid :goal-id] "")
       (assoc-in [:select-bid :bid-id] ""))))

(reg-event-fx
 :ui.select-bid-dialog/ok
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [goal-id bid-id]]
   {:dispatch-n [[:blockchain.select-bid/send goal-id bid-id]
                 [:ui.select-bid-dialog/toggle-view]
                 [:ui.select-bid-dialog/clear-selected]]}))

(reg-event-fx
 :ui.select-bid-dialog/cancel
 (interceptors-fx :spec false)

 (fn [{:keys [db]}]
   {:dispatch-n [[:ui.select-bid-dialog/toggle-view]
                 [:ui.select-bid-dialog/clear-selected]]}))

;;;
;;;
;;; VIEW GOAL DIALOG
;;;
;;;
(reg-event-db
 :ui.view-goal-dialog/toggle-view
 (interceptors)
 (fn [db]
   (update-in db [:view-goal :dialog-open?] not)))

(reg-event-db
 :ui.view-goal-dialog/set-goal
 (fn [db [_ goal-id]]
   (assoc-in db [:view-goal :goal-id] goal-id)))

(reg-event-db
 :ui.view-goal-dialog/on-view-goal-page

 (fn [db [_ on-view-goal-page?]]
   (assoc-in db [:view-goal :on-view-goal-page?] on-view-goal-page?)))

(reg-event-db
 :ui.view-goal-dialog/clear-goal
 (fn [db]
   (assoc-in db [:view-goal :goal-id] "")))

(reg-event-fx
 :ui.view-goal-dialog/open
 (interceptors-fx :spec false)
 (fn [{:keys [db]} [goal-id]]
   ;; (js/console.log :debug :open-view-goal goal-id)
   {:dispatch-n [[:ui.view-goal-dialog/set-goal goal-id]
                 [:ui.view-goal-dialog/toggle-view]]}))

(reg-event-fx
 :ui.view-goal-dialog/close
 (interceptors-fx :spec false)
 (fn [{:keys [db]}]
   (let [on-view-goal-page? (get-in db [:view-goal :on-view-goal-page?])]
     {:dispatch-n (cond-> [[:ui.view-goal-dialog/toggle-view]
                           [:ui.view-goal-dialog/clear-goal]
                           [:ui.view-goal-dialog/on-view-goal-page false]]
                    on-view-goal-page?
                    (conj [:ui.go-to-menu :pulse]))})))

;;;
;;; VIEW GOAL PAGE
;;;
(reg-event-fx
 :ui.view-goal-page/init
 (interceptors-fx :spec false)
 (fn [{:keys [db]}]
   (js/console.log :ui.view-goal-page)
   (let [goal-id (get-in db [:current-page :route-params :goal-id])]
     {:dispatch-later [{:ms 50
                        :dispatch [:ui.view-goal-dialog/on-view-goal-page true]}
                       {:ms 500
                        :dispatch [:ui.view-goal-dialog/open goal-id]}]})))

;;;
;;; HOW TO PLAY PAGE
;;;
(reg-event-db
 :ui.how-to-play/init
 (interceptors)

 (fn [db]
   (assoc-in db [:how-to-play :step] 0)))

(reg-event-db
 :ui.how-to-play/next-step
 (interceptors)

 (fn [db]
   (let [steps-count (get-in db [:how-to-play :steps-count])
         current-step (get-in db [:how-to-play :step])
         next-step (inc current-step)
         can-go? (< next-step steps-count)]
     (cond-> db
       can-go?
       (assoc-in [:how-to-play :step] next-step)))))

(reg-event-db
 :ui.how-to-play/previous-step
 (interceptors)

 (fn [db]
   (let [current-step (get-in db [:how-to-play :step])
         previous-step (dec current-step)
         can-go? (>= previous-step 0)]
     (cond-> db
       can-go?
       (assoc-in [:how-to-play :step] previous-step)))))

(reg-event-db
 :ui.how-to-play/set-step
 (interceptors)

 (fn [db [step]]
   (let [steps-count (get-in db [:how-to-play :steps-count])
         can-set? (and (>= step 0)
                       (< step steps-count))]
     (cond-> db
       can-set?
       (assoc-in [:how-to-play :step] step)))))

;;;
;;; drawer
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
   (u/scroll-to-bottom "chat-messages-container" 200)
   {}))

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

    :ga/page-view [(apply pages/path-for (:handler match) (flatten (into [] (:route-params match))))]
    :dispatch [:ui.page.title/update]}))

;;;
;;; change location
;;;
(reg-event-fx
 :ui.change-location
 (interceptors-fx :spec false)

 (fn [_ [& route-params]]
   (js/console.log :change-location)
   (aset js/window "location" (apply pages/path-for route-params))
   {}))

;;;
;;; go to menu link
;;;
(reg-event-fx
 :ui.go-to-menu
 (interceptors-fx :spec false)

 (fn [_ [menu-page-key]]
   (let [menu-link-selector (str ".menu-link-" (subs (str menu-page-key) 1))]
    (.click (js/document.querySelector menu-link-selector)))))

;;;
;;; force set window size
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

;;;
;;;
;;; APP MODES
;;;
;;;
(reg-event-fx
 :app/critical-error
 (interceptors-fx :spec true)

 (fn [{:keys [db]}]
   {:db (-> db
            (assoc :critical-error? true)
            (assoc :force-read-only? true))

    :dispatch [:sync-db/fetch]}))

(reg-event-fx
 :app/recover-after-critical-error
 (interceptors-fx :spec true)

 (fn [{:keys [db]}]
   {:db (-> db
            (assoc :critical-error? false)
            (assoc :force-read-only? true))}))
