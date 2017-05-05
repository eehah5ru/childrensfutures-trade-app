(ns childrensfutures-trade.components.goals
  (:require
   [cljs-react-material-ui.icons :as icons]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.core :refer [color]]
   [medley.core :as medley]
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as r]
   [clavatar-js.core :as clavatar]

   [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
   [childrensfutures-trade.styles :as st]
   [childrensfutures-trade.goal-stages :as gs]
   [childrensfutures-trade.pages :as pages]
   [childrensfutures-trade.utils :as u]

   [childrensfutures-trade.components.avatars :refer [goal-avatar]]
   [childrensfutures-trade.components.goal-statuses :as statuses]

   ;; [childrensfutures-trade.pages :as pages]
   ;;
   ;; goal views
   ;;
   [childrensfutures-trade.components.goal.created :as created-goal]
   [childrensfutures-trade.components.goal.unknown :as unknown-goal]
   [childrensfutures-trade.components.goal.cancelled :as cancelled-goal]
   [childrensfutures-trade.components.goal.bid-placed :as bid-placed-goal]
   [childrensfutures-trade.components.goal.bid-selected :as bid-selected-goal]
   [childrensfutures-trade.components.goal.investment-sent :as investment-sent-goal]
   [childrensfutures-trade.components.goal.investment-received :as investment-received-goal]
   [childrensfutures-trade.components.goal.achieved :as achieved-goal]
   [childrensfutures-trade.components.goal.bonus-asked :as bonus-asked-goal]
   [childrensfutures-trade.components.goal.bonus-sent :as bonus-sent-goal]
   [childrensfutures-trade.components.goal.completed :as completed-goal])
  )




;;;
;;;
;;; BIDS VIEWS
;;;
;;;

;;;
;;; PLACE BID
;;;
(defn- place-bid-view []
  (let [new-bid (subscribe [:db.bids/new-bid])]
    [:div
     [:h1 "New Bid"]
     [ui/text-field {:default-value (:description @new-bid)
                     :on-change #(dispatch [:place-bid/update (:goal-id new-bid) :description (u/evt-val %)])
                     :name "description"
                     :error-text "Suggest something that might interest the dream keeper! What might this person need? What would you like to give in exchange? It could be a secret about her/his friend or a material gift! You can send it or hide it near the district a person lives in."
                     :error-style {:color (color :grey-600)}
                     :max-length 2000 ;FIXME
                     :floating-label-text "Bid's description"
                     :style {:width "100%"}}]]))


(defn place-bid-dialog []
  (let [show-new-bid? (subscribe [:ui/show-new-bid?])
        new-bid (subscribe [:db.bids/new-bid])
        place-button [ui/raised-button
                      {:secondary true
                       :disabled (empty? (:description @new-bid))
                       :label "Invest"
                       :on-touch-tap #(dispatch
                                       [:place-bid/place (:goal-id @new-bid)])}]
        cancel-button [ui/flat-button
                       {:secondary true
                        :disabled false
                        :label "cancel"
                        :on-touch-tap #(dispatch [:place-bid/cancel (:goal-id @new-bid)])}]]
    [ui/dialog
     {:modal true
      :actions [(r/as-element place-button) (r/as-element cancel-button)]
      :open @show-new-bid?}

     [place-bid-view]]))


(defn confirm-bid-selection-dialog []
  (let [selected-bid (subscribe [:ui.select-bid/selected])
        {:keys [goal-id bid-id]} @selected-bid
        dialog-open? (subscribe [:ui.select-bid/dialog-open?])
        ok-button [ui/raised-button
                   {:secondary true
                    :disabled false
                    :label "Do it!"
                    :style {:margin-top 20}
                    :on-touch-tap #(dispatch [:ui.select-bid-dialog/ok goal-id bid-id])}]
        cancel-button [ui/flat-button
                       {:secondary true
                        :disabled false
                        :label "cancel"
                        :on-touch-tap #(dispatch [:ui.select-bid-dialog/cancel])}]]
    [ui/dialog
     {:modal true
      :actions [(r/as-element ok-button)
                (r/as-element cancel-button)]
      :open @dialog-open?}

     [:h2 "Hey! Don’t look at my appearance! Once you have sliced me, you can’t make me go back!"]]))

;;; staged view properties
(defn- staged-card-properties [stage]
  (cond
    (gs/unknown? stage) unknown-goal/card-properties
    (gs/created? stage) created-goal/card-properties
    (gs/cancelled? stage) cancelled-goal/card-properties
    (gs/bid-placed? stage) bid-placed-goal/card-properties
    (gs/bid-selected? stage) bid-selected-goal/card-properties
    (gs/investment-sent? stage) investment-sent-goal/card-properties
    (gs/investment-received? stage) investment-received-goal/card-properties
    (gs/goal-achieved? stage) achieved-goal/card-properties
    (gs/bonus-asked? stage) bonus-asked-goal/card-properties
    (gs/bonus-sent? stage) bonus-sent-goal/card-properties
    (gs/goal-completed? stage) completed-goal/card-properties
    :else unknown-goal/card-properties))

(defn staged-goal-card-class-name [stage]
  (str "goal-stage-" (subs (str stage) 1)))

;;;
;;;
;;; staged goal views
;;;
;;;
(defn staged-goal-view [goal & {:keys [expanded?
                                       show-expandable-button?]
                                :or {expanded? false
                                     show-expandable-button? true}}]
  (let [{:keys [goal-id
                stage
                description
                give-in-return
                trx-on-air?]} goal
        show-share-url? (subscribe [:ui.goal/show-share-url? goal-id])
        show-splash? (subscribe [:ui.goal/show-trx-on-air-splash? goal-id])
        card-properties (staged-card-properties stage)
        location (subscribe [:location/root])
        {:keys [card-style
                card-text
                card-actions
                card-subtitle-extra
                goal-statuses-extra]} card-properties]
    [ui/card
     {:class-name (str "goal-card" " " (staged-goal-card-class-name stage))
      :style (card-style goal)
      ;; :expanded expanded?
      :initially-expanded expanded?}

     ;; trx on air spinner
     (when @show-splash?
       [:div
        {:class "trx-on-air-splash"}])

     (when @show-splash?
       [:div
        {:class "trx-on-air-spinner"}
        [ui/circular-progress
         {:mode :indeterminate
          :color (color :cyan-50)
          :thickness 10
          :size 80}]])

     ;; header
     [ui/card-header
      {:title (r/as-element [:span
                             description])
       :subtitle (r/as-element [:span
                                [:em "Bonus: "]
                                give-in-return
                                (when card-subtitle-extra
                                  [:br])
                                (when card-subtitle-extra
                                  [card-subtitle-extra goal])
                                [:br]
                                (statuses/render
                                 (statuses/goal-statuses goal
                                                         goal-statuses-extra))])
       :show-expandable-button show-expandable-button?
       :act-as-expander show-expandable-button?
       :avatar (r/as-element [goal-avatar goal])}]

     ;; text
     [ui/card-text
      {:expandable true
       :style {:position "relative"}
       :children (cond-> []
                   true
                   (conj [:span
                          {:style {:display "none"}}
                          :id (str "goal-" goal-id)])

                   true
                   (conj [ui/floating-action-button
                          {:z-depth 1
                           :mini true
                           :secondary true
                           :children (icons/social-share)
                           :class-name "clipboard-button"
                           :on-touch-tap #(dispatch [:ui.goal/toggle-share-url goal-id])
                           :style {:position "absolute"
                                   :top -5
                                   :right 30}}])

                   true
                   (conj [ui/divider])

                   @show-share-url?
                   (conj [ui/text-field
                          {:value (str @location (subs (pages/path-for :view-goal :goal-id goal-id) 1))
                           :name "share-link"
                           :full-width true
                           :floating-label-fixed true
                           :floating-label-text "copy and share this url"}])

                   true
                   (conj [card-text goal])

                   true
                   (as-> cs (map #(r/as-element (with-meta %1 {:key %2}))
                                 cs
                                 (range))))}]

     ;; actions
     [ui/card-actions
      {:act-as-expander false
       :expandable true
       :children (map #(r/as-element %) (card-actions goal))}
      ]]))

;;;
;;;
;;; select goal-view
;;;
;;;

;;;
;;; goals list view
;;;
(defn goals-view [goals-selector & {:keys [no-goals-view]}]
  (when (nil? no-goals-view)
    (throw "no-goals-view is nil!!!"))

  (let [goals (goals-selector)]
    (if (empty? @goals)
      ;; empty goals notification
      [no-goals-view]
      ;; not empty
      [:div (for [goal @goals]
              ^{:key (:goal-id goal)} [staged-goal-view goal])])
    ))

(defn view-goal-dialog []
  (let [goal-id (subscribe [:ui.view-goal/goal-id])
        goal (subscribe [:db.goals/get @goal-id])
        dialog-open? (subscribe [:ui.view-goal/dialog-open?])]
    [ui/dialog
     {:body-class-name "view-goal-dialog"
      :modal false
      :open @dialog-open?
      :on-request-close #(dispatch [:ui.view-goal-dialog/close])}
     [staged-goal-view @goal :expanded? true :show-expandable-button? false]]))
