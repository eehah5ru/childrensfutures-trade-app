(ns childrensfutures-trade.components.goal.common
  (:require
   [cljs-react-material-ui.icons :as icons]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.core :refer [color]]
   [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
   [childrensfutures-trade.styles :as st]
   [childrensfutures-trade.utils :as u]
   [medley.core :as medley]
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as r]
   [clavatar-js.core :as clavatar]

   [childrensfutures-trade.components.avatars :refer [bid-avatar]]
   [childrensfutures-trade.components.goal.utils :as gu]))



(def default-chat-open-button-title "Discuss details")

;;;
;;; card extra subtitle
;;;
(defn investment-subtitle [goal]
  (let [selected-bid (subscribe [:db.goal.bids/selected (:goal-id goal)])]
    [:span "Investment: " (:description @selected-bid)]))

;;;
;;; simple card title
;;;
(defn simple-card-title [title]
  [ui/card-title
   {:title (r/as-element title)}])

;;;
;;; card button
;;;
(defn card-flat-button [props]
  (let [read-only-app? (subscribe [:app/read-only?])]
    (fn [props]
      [ui/flat-button
       (r/merge-props
        {:secondary true
         :disabled @read-only-app?
         :style st/goal-card-button}
        props)])))

(defn card-raised-button [props]
  (let [read-only-app? (subscribe [:app/read-only?])]
    (fn [props]
      [ui/raised-button
       (r/merge-props
        {:secondary false
         :disabled @read-only-app?
         :style st/goal-card-button}
        props)])))

;;;
;;; chat button
;;;
(defn chat-open-button
  ([ids]
   (chat-open-button ids default-chat-open-button-title))

  ([ids title]
   (let [read-only-app? (subscribe [:app/read-only?])]
     [card-raised-button
      {:secondary true
       :label title
       :on-touch-tap #(do
                        (dispatch [:messages/set-channel-id ids])
                        (dispatch [:ui.chat/toggle-view]))
       }])))


(defn owner-investor-chat-button [goal-id]
  (let [bid (subscribe [:db.goal.bids/selected goal-id])
        bid-id (:bid-id @bid)]
    (chat-open-button [goal-id bid-id])))

;;;
;;;
;;; goal bids view
;;;
;;;

;;; bid view
;;;
;;; BID LIST ITEM
;;;
(defn- bid-list-item [goal-id bid]
  (let [{:keys [owner description selected?]} bid
        bid-id owner]
    [ui/list-item
     {:left-avatar (r/as-element [bid-avatar bid])
      ;;:right-icon (icons/action-info)
      :primary-text (:description bid)
      :right-toggle (r/as-element
                     [ui/toggle
                      {:toggled selected?
                       :disabled false
                       :on-toggle #(do
                                     (dispatch [:ui.select-bid-dialog/set-selected goal-id bid-id])
                                     (dispatch [:ui.select-bid-dialog/toggle-view]))
                       }])}]))

(defn goal-bids-view [goal]
  (let [goal-id (:goal-id goal)
        bids (subscribe [:db.goal.bids/sorted goal-id])]

    [ui/list
     (for [bid @bids]
       ^{:key (:owner bid)}
       [bid-list-item goal-id bid])]))

;;;
;;; GENERIC NO CONTENT VIEW
;;;
(defn generic-no-content-view [& {:keys [message
                                         button?
                                         button-label
                                         button-action]}]
  (let [read-only-app? (subscribe [:app/read-only?])] [:div
           {:style {:margin-top 10
                    :text-align "center"}}
           [:h2 message]
           (when button?
             [ui/raised-button
              {:style {:margin-top 20}
               :label button-label
               :disabled @read-only-app?
               :primary true
               ;; :icon (icons/content-add-circle) :label-position "before"
               :on-touch-tap button-action
               }])]))


;;;
;;;
;;; NO GOALS VIEW
;;;
;;;
(defn no-goals-view []
  (generic-no-content-view
   :message "No goals at the moment?! Oh my! Create your future now!"
   :button? true
   :button-label "Tell about your dream!"
   :button-action #(dispatch [:ui.new-goal/toggle-view])))

;;;
;;;
;;; NO MY INVESTMENTS VIEW
;;;
;;; Be a good person – help someone out!
(defn no-investments-view []
  (generic-no-content-view
   :message "No goals at the moment?! Oh my! Create your future now!"
   :button? true
   :button-label "Be a good person – help someone out!"
   :button-action #(dispatch [:ui.go-to-menu :pulse])))
