(ns childrensfutures-trade.components.pulse-page
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


   [childrensfutures-trade.goal-stages :as gs]
   [childrensfutures-trade.components.goal-statuses :as statuses]

   [childrensfutures-trade.components.goals :refer [goals-view]]))

;;;
;;;
;;; pulse event colors
;;;
;;;
(defn pulse-event-color [event state]
  (let [colors {:goal-added {:normal (color :green-200)
                             :hover (color :green-500)}
                :investment-placed {:normal (color :lime-200)
                                    :hover (color :lime-500)}}]
    (get-in colors [event state] (color :red-500))))

;;;
;;;
;;; action icons
;;;
;;;
(defn created-icon-button [goal-id]
  (let [role (subscribe [:role/role goal-id])
        read-only-app? (subscribe [:app/read-only?])
        icon-options {:disabled false
                      :tooltip "Invest now!"
                      :touch true
                      :on-touch-tap #(dispatch [:place-bid/show-new-bid goal-id])}
        icon (fn [& {:keys [disabled?]}]
               [ui/icon-button
                (merge icon-options
                       {:disabled disabled?})
                (icons/action-trending-up)])]
    (condp = @role
      :stranger  (icon :disabled? @read-only-app?)
      (icon :disabled? true))))

(defn bid-placed-icon-button [goal-id]
  (let [role (subscribe [:role/role goal-id])
        read-only-app? (subscribe [:app/read-only?])
        icon (fn [& {:keys [disabled?]}]
               [ui/icon-button
                {:disabled disabled?
                 :tooltip "Invest also!"
                 :touch true
                 :on-touch-tap #(dispatch [:place-bid/show-new-bid goal-id])}
                (icons/action-trending-up)])]
    (condp = @role
      :stranger (icon :disabled? @read-only-app?)
      (icon :disabled? true))))

(defn unknown-stage-icon-button [goal-id]
  [ui/icon-button
   {:disabled true
    :tooltip "Invest also!"
    :touch true
    }
   (icons/action-trending-up)])

(defn- staged-icon-button [goal-id]
  (let [stage (subscribe [:db.goal/stage goal-id])]
    (condp gs/stage? @stage
      :created (created-icon-button goal-id)
      :bid-placed (bid-placed-icon-button goal-id)
      (unknown-stage-icon-button goal-id))))

;;;
;;;
;;; event views
;;;
;;;
(defn- goal-added-event-view [e]
  (let [goal-id (:goal-id e)
        event-number (:number e)
        goal (subscribe [:db.goals/get goal-id])
        {:keys [description give-in-return]} @goal
        my-goal? (subscribe [:db.goal/my? goal-id])
        stage (subscribe [:db.goal/stage goal-id])
        disabled? (or (not (gs/stage? :created @stage))
                      @my-goal?)]

    [ui/list-item
     {
      ;; :style {:background-color (pulse-event-color :goal-added :normal)
      ;;         :margin-bottom 2}
      :class-name "pulse-event pulse-goal-added"
      ;; :hover-color (pulse-event-color :goal-added :hover)
      :primary-text description
      :secondary-text (r/as-element
                       [:span
                        {:style {:display "inline-block"
                          :max-width "100%"}}
                        (str "Bonus: " give-in-return)])
      :right-icon-button (r/as-element
                          (staged-icon-button goal-id))
      :on-touch-tap #(dispatch [:ui.view-goal-dialog/open goal-id])}
     ]))

(defn- investment-placed-event-view [e]
  (let [goal-id (:goal-id e)
        bid-id (:bid-id e)
        event-number (:number e)
        goal (subscribe [:db.goals/get goal-id])
        {:keys [description give-in-return]} @goal
        stranger? (subscribe [:role/stranger? goal-id])
        stage (subscribe [:db.goal/stage goal-id])
        disabled? (or (not @stranger?)
                      (not (gs/stage? :bid-placed @stage)))]

    [ui/list-item
     {:class-name "pulse-event pulse-investment-placed"
      :primary-text description
      :secondary-text (r/as-element
                       [:span
                        {:style {:display "inline-block"
                                 :max-width "100%"}}
                        (str "Bonus: " give-in-return)])
      :right-icon-button (r/as-element
                          (staged-icon-button goal-id))
      :on-touch-tap #(dispatch [:ui.view-goal-dialog/open goal-id])}]))

;; (defn- bid-selected-event-view [e]
;;   [:h3
;;    "Bid Selected"])

(defn- unknown-event-view [e]
  [:h3
   (str "Unknown event type " (:type e))])


(defn ^:export pulse-page []
  (let [events (subscribe [:db.pulse/all-events])]
    [outer-paper
     [ui/list
      (for [event @events]
        (let [{:keys [type goal-id number]} event]
          (with-meta (condp = (:type event)
                       :goal-added [goal-added-event-view event]
                       :investment-placed [investment-placed-event-view event]
                       :else [unknown-event-view event])
            {:key (str type "-" goal-id "-" number)})))]]))
