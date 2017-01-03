(ns childrensfutures-trade.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [childrensfutures-trade.address-select-field :refer [address-select-field]]
    [cljs-react-material-ui.reagent :as ui]
    [cljs-react-material-ui.core :refer [get-mui-theme color]]
    [childrensfutures-trade.utils :as u]
    [childrensfutures-trade.subs :as s]))

(def col (r/adapt-react-class js/ReactFlexboxGrid.Col))
(def row (r/adapt-react-class js/ReactFlexboxGrid.Row))

(defn- new-goal-component []
  (let [new-goal (subscribe [:db/new-goal])
        my-addresses (subscribe [:db/my-addresses])
        balance (subscribe [:new-goal/selected-address-balance])]
    (fn []
      [row
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
        [ui/paper {:style {:padding "0 20px 20px"}}
         [ui/text-field {:default-value (:description @new-goal)
                         :on-change #(dispatch [:new-goal/update :description (u/evt-val %)])
                         :name "description"
                         :max-length 120 ;FIXME
                         :floating-label-text "Goal's description"
                         :style {:width "100%"}}]
         [:br]

         [address-select-field
          @my-addresses
          (:address @new-goal)
          [[:new-goal/update :owner] [:current-address/update]]]
         [:br]
         [:h3 "Balance: " (u/eth @balance)]
         [:br]
         [ui/raised-button
          {:secondary true
           :disabled (or (empty? (:description @new-goal))
                         (empty? (:address @new-goal))
                         (:sending? @new-goal))
           :label "Place on Exchange"
           :style {:margin-top 15}
           :on-touch-tap #(dispatch [:new-goal/send])}]]]])))

;;;
;;; NEW BID VIEW
;;;
(defn- new-bid-view [goal-id]
  (let [new-bid (subscribe [:db/new-bid goal-id])]
    [:div
     [:h3 "Place Bid"]
     [ui/text-field {:default-value (:description @new-bid)
                     :on-change #(dispatch [:place-bid/update goal-id :description (u/evt-val %)])
                     :name "description"
                     :max-length 120    ;FIXME
                     :floating-label-text "Bid's description"
                     :style {:width "100%"}}]
     [:br]
     [ui/raised-button
      {:secondary false
       :disabled (empty? (:description @new-bid))
       :label "Place new bid"
       :on-touch-tap #(dispatch [:place-bid/send goal-id])}]
     [ui/flat-button
      {:secondary true
       :disabled false
       :label "cancel"
       :on-touch-tap #(dispatch [:place-bid/cancel goal-id])}]]))


;;;
;;; goal view
;;;
(defn- goal-component [goal]
  (let [{:keys [goal-id owner description cancelled? cancelling?]} goal
        show-new-bid? (subscribe [:db/show-new-bid? goal-id])]
    [:div {:style {:margin-top 20}
           :key goal-id}
     [:h3
      description
      [ui/flat-button
       {:secondary true
        :disabled (or cancelled?
                      cancelling?
                      (not (= @(subscribe [:db/current-address]) owner)))
        :label "Delete"
        :on-touch-tap #(dispatch [:cancel-goal/send goal-id])}]

      [ui/flat-button
       {:secondary false
        :disabled false
        :label "place bid"
        :on-touch-tap #(dispatch [:place-bid/show-new-bid goal-id])}]]

     [:div {:style {:margin-top 5}}
      "goalId: "
      goal-id
      ]
     [:div {:style {:margin-top 5}}
      "owner: "
      owner]
     (when @show-new-bid?
       (new-bid-view goal-id))
     [ui/divider]]))


;;;
;;; goals list view
;;;
(defn- goals-component []
  (let [goals (subscribe [:db/goals])]
    (fn []
      [row
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
        [ui/paper {:style {:padding 20 :margin-top 20}}
         [:h1 "Goals"]
         (for [goal @goals]
           ^{:key (:goal-id goal)} [goal-component goal])
         ]]])))

(defn main-panel []
  (let []
    (fn []
      [ui/mui-theme-provider
       {:mui-theme (get-mui-theme {:palette {:primary1-color (color :light-blue500)
                                             :accent1-color (color :amber700)}})}
       [:div
        [ui/app-bar {:title "Goals Exchange Market"}]
        [new-goal-component]
        [goals-component]]])))
