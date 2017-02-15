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

   [childrensfutures-trade.components.goal.common :refer [generic-no-content-view]]))

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


(defn- staged-disabled-event-view [e e-class]
  (let [goal-id (:goal-id e)
        event-number (:number e)
        goal (subscribe [:db.goals/get goal-id])
        bid (subscribe [:db.goal.bids/selected goal-id])
        {:keys [description give-in-return]} @goal
        stage (subscribe [:db.goal/stage goal-id])
        ]
    [ui/list-item
     {:class-name (str "pulse-event " e-class)
      :primary-text description
      :secondary-text (r/as-element
                       [:span
                        {:style {:display "inline-block"
                                 :max-width "100%"}}
                        (str "Bonus: " give-in-return)
                        [:br]
                        (str "Investment: " (:description @bid))])
      :right-icon-button (r/as-element
                          (staged-icon-button goal-id))}]))

(defn investment-selected-event-view [e]
  (staged-disabled-event-view e "goal-stage-bid-selected"))

(defn investment-sent-event-view [e]
  (staged-disabled-event-view e "goal-stage-investment-sent"))

(defn investment-received-event-view [e]
  (staged-disabled-event-view e "goal-stage-investment-received"))

(defn goal-achieved-event-view [e]
  (staged-disabled-event-view e "goal-stage-goal-achieved"))

(defn bonus-asked-event-view [e]
  (staged-disabled-event-view e "goal-stage-bonus-asked"))

(defn bonus-sent-event-view [e]
  (staged-disabled-event-view e "goal-stage-bonus-sent"))

(defn goal-completed-event-view [e]
  (staged-disabled-event-view e "goal-stage-goal-completed"))

(defn goal-cancelled-event-view [e]
  (staged-disabled-event-view e "goal-stage-goal-cancelled"))



;; (defn- bid-selected-event-view [e]
;;   [:h3
;;    "Bid Selected"])

(defn- unknown-event-view [e]
  [:h3
   (str "Unknown event type " (:type e))])


(defn ^:export pulse-page []
  (let [events (subscribe [:db.pulse/all-events])]
    [outer-paper
     (if (empty? @events)
       ;; empty
       (generic-no-content-view
        :message "Pulse rate is slowing. Future is gonna struggle to breathe."
        :button? false)
       ;; ok
       [ui/list
        (for [event @events]
          (let [{:keys [type goal-id number]} event]
            (with-meta (condp = (:type event)
                         :goal-added [goal-added-event-view event]
                         :investment-placed [investment-placed-event-view event]
                         :investment-selected [investment-selected-event-view event]
                         :investment-sent [investment-sent-event-view event]
                         :investment-received [investment-received-event-view event]
                         :goal-achieved [goal-achieved-event-view event]
                         :bonus-asked [bonus-asked-event-view event]
                         :bonus-sent [bonus-sent-event-view event]
                         :goal-completed [goal-completed-event-view event]
                         :goal-cancelled [goal-cancelled-event-view event]
                         :else [unknown-event-view event])
              {:key (str type "-" goal-id "-" number)})))])]))
