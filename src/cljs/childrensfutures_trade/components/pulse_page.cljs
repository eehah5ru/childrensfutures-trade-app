(ns childrensfutures-trade.components.pulse-page
  (:require
    [cljs-react-material-ui.icons :as icons]
    [cljs-react-material-ui.reagent :as ui]
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
;;; action icons
;;;
;;;
(defn created-icon-button [goal-id]
  (let [role (subscribe [:role/role goal-id])]
    (condp = @role
      :stranger  [ui/icon-button
                           {:disabled false
                            :tooltip "Invest now!"
                            :touch true
                            :on-touch-tap #(dispatch [:place-bid/show-new-bid goal-id])}
                  (icons/action-trending-up)]
      nil)))

(defn bid-placed-icon-button [goal-id]
  (let [role (subscribe [:role/role goal-id])]
    (condp = @role
      :stranger [ui/icon-button
                           {:disabled false
                            :tooltip "Invest also!"
                            :touch true
                            :on-touch-tap #(dispatch [:place-bid/show-new-bid goal-id])}
                 (icons/social-plus-one)]
      nil)))

(defn- staged-icon-button [goal-id]
  (let [stage (subscribe [:db.goal/stage goal-id])]
    (condp gs/stage? @stage
      :created (created-icon-button goal-id)
      :bid-placed (bid-placed-icon-button goal-id)
      nil)))

;;;
;;;
;;; event views
;;;
;;;
(defn- goal-added-event-view [e]
  (let [goal-id (:goal-id e)
        goal (subscribe [:db.goals/get goal-id])
        {:keys [description give-in-return]} @goal
        my-goal? (subscribe [:db.goal/my? goal-id])
        stage (subscribe [:db.goal/stage goal-id])
        disabled? (or (not (gs/stage? :created @stage))
                      @my-goal?)]
    ^{:key (str :goal-added- goal-id)}
    [ui/list-item
     {:primary-text (str "Goal Added: " description)
      :secondary-text (r/as-element
                       [:span (str "Bonus: " give-in-return)
                        [:br]
                        (statuses/render (statuses/staged-goal-statuses @goal))])
      :right-icon-button (r/as-element
                          (staged-icon-button goal-id))
      :on-touch-tap #(dispatch [:ui.view-goal-dialog/open goal-id])}
     ]))

(defn- investment-placed-event-view [e]
  (let [goal-id (:goal-id e)
        goal (subscribe [:db.goals/get goal-id])
        {:keys [description give-in-return]} @goal
        stranger? (subscribe [:role/stranger? goal-id])
        stage (subscribe [:db.goal/stage goal-id])
        disabled? (or (not @stranger?)
                      (not (gs/stage? :bid-placed @stage)))]
    ^{:key (str :investment-placed- goal-id)}
    [ui/list-item
     {:primary-text (str "Investment placed!: " description)
      :secondary-text (r/as-element
                       [:span (str "Bonus: " give-in-return)
                        [:br]
                        (statuses/render (statuses/staged-goal-statuses @goal))])
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
        (let [{:keys [type goal-id]} event]
          (with-meta (condp = (:type event)
                       :goal-added [goal-added-event-view event]
                       :investment-placed [investment-placed-event-view event]
                       :else [unknown-event-view event])
            {:key (str type "-" goal-id)})))]]))
