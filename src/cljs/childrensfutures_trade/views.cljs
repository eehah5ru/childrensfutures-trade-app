(ns childrensfutures-trade.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [childrensfutures-trade.address-select-field :refer [address-select-field]]
    [cljs-react-material-ui.reagent :as ui]
    [cljs-react-material-ui.core :refer [get-mui-theme color]]
    [childrensfutures-trade.utils :as u]))

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
                         :max-length 120
                         :floating-label-text "Goal's description"
                         :style {:width "100%"}}]
         [:br]

         [address-select-field
          @my-addresses
          (:address @new-goal)
          [:new-goal/update :address]]
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

(defn- goals-component []
  (let [goals (subscribe [:db/goals])]
    (fn []
      [row
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
        [ui/paper {:style {:padding 20 :margin-top 20}}
         [:h1 "Goals"]
         (for [{:keys [goal-id owner description]} @goals]
           [:div {:style {:margin-top 20}
                  :key goal-id}
            [:h3 owner]
            [:div {:style {:margin-top 5}}
             description]
            [:h3 {:style {:margin "5px 0 10px"}}
             owner]
            [ui/divider]])]]])))

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
