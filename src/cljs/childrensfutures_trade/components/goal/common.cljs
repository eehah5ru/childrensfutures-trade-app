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
   [childrensfutures-trade.components.goal.utils :as gu]

   ))



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
   {:title title}])

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
;;; share goal button
;;;
;;;
(defn share-goal-button [goal-id]
  )

;;;
;;; empty actions
;;;
(def empty-actions (constantly []))
