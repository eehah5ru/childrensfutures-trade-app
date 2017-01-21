(ns childrensfutures-trade.components.goals
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
    [clavatar-js.core :as clavatar]))

;;;
;;; NEW BID VIEW
;;;
(defn- new-bid-view [goal-id]
  ;; FIXME move nested code to fn!!!
  (let [new-bid (subscribe [:db/new-bid goal-id])]
    [:div
     [:h3 "Place Bid"]
     [ui/text-field {:default-value (:description @new-bid)
                     :on-change #(dispatch [:place-bid/update goal-id :description (u/evt-val %)])
                     :name "description"
                     :max-length 120 ;FIXME
                     :floating-label-text "Bid's description"
                     :style {:width "100%"}}]
     [:br]
     ;;
     ;; place
     ;;
     [ui/raised-button
      {:secondary false
       :disabled (empty? (:description @new-bid))
       :label "Place new bid"
       :on-touch-tap #(dispatch [:place-bid/send goal-id])}]
     ;;
     ;; cancel
     ;;
     [ui/flat-button
      {:secondary true
       :disabled false
       :label "cancel"
       :on-touch-tap #(dispatch [:place-bid/cancel goal-id])}]

     [ui/snackbar {:message "Placing bid"
                   :open (:placing? @new-bid)}]]))

;;;
;;; BID VIEW
;;;
(defn- bid-view [goal-id bid-id bid]
  (let [{:keys [owner description selected?]} bid
        my-bid? (subscribe [:db/my-bid? goal-id bid-id])
        my-goal? (subscribe [:db/my-goal? goal-id])
        goal-has-selected-bid? (subscribe [:db/goal-has-selected-bid? goal-id])]
    (fn []
      [:div.bid
       [:h4 description]
       ;;
       ;; select bid
       ;;
       [ui/flat-button
        {:secondary false
         :disabled (or @my-bid?
                       (not @my-goal?)
                       @goal-has-selected-bid?)
         :label "select"
         :on-touch-tap #(dispatch [:select-bid/send goal-id bid-id])}]

       ;;
       ;; cancel bid
       ;;
       [ui/flat-button
        {:secondary false
         :disabled (or (not @my-bid?)
                       @goal-has-selected-bid?)
         :label "delete"
         :on-touch-tap #(dispatch [:cancel-bid/send goal-id bid-id])}]

       [ui/divider]])))

;;;
;;; goal view
;;; OLD!
;;;
(defn- goal-component1 [goal]
  (let [{:keys [goal-id owner description cancelled? cancelling? show-details?]} goal
        show-new-bid? (subscribe [:db/show-new-bid? goal-id])
        my-goal? (subscribe [:db/my-goal? goal-id])
        bids (subscribe [:db/sorted-bids goal-id])
        already-bidded? (subscribe [:db/already-bidded? goal-id])]
    [:div {:style {:margin-top 20}
           :key goal-id}
     [:h3
      description]

     ;;
     ;; place bid
     ;;
     [ui/flat-button
      {:secondary false
       :disabled (or @my-goal?
                     @already-bidded?)
       :label "place bid"
       :on-touch-tap #(dispatch [:place-bid/show-new-bid goal-id])}]
     ;;
     ;; show details
     ;; FIXME: for debug only
     ;;
     [ui/flat-button
      {:secondary true
       :disabled false
       :label (if show-details?
                "hide details"
                "show details")
       :on-touch-tap #(dispatch [:goal/toggle-details goal-id])}]

     ;;
     ;; cancel goal
     ;;
     [ui/flat-button
      {:secondary true
       :disabled (or cancelled?
                     cancelling?
                     (not (= @(subscribe [:db/current-address]) owner)))
       :label "Delete"
       :on-touch-tap #(dispatch [:cancel-goal/send goal-id])}]


    ;;
    ;; details view
    ;;
    (when show-details?
      [:div.goal-details
       [:div {:style {:margin-top 5}}
        "goalId: "
        goal-id]
       [:div {:style {:margin-top 5}}
        "owner: "
        owner]])
    ;;
    ;; new bid view
    ;;
    (when @show-new-bid?
      (new-bid-view goal-id))

    (when (not (empty? @bids))
      [:h3 "Bids"])
    (for [bid @bids]
      ^{:key (:owner bid)} [bid-view goal-id (:owner bid) bid]) ;FIXME somehow use bid-id here

    [ui/divider]]))


;;;
;;;
;;; GOAL VIEW
;;;
;;;
(defn- goal-avatar [goal]
  [ui/avatar {:style {:margin-top 5
                      :margin-right 20}
              :src (clavatar/gravatar (:owner goal))}])


;;;
;;; card actions
;;;
(defn- goal-actions [goal]
  (let [{:keys [goal-id show-details? cancelled? cancelling?]} goal
        my-goal? (subscribe [:db/my-goal? goal-id])
        already-bidded? (subscribe [:db/already-bidded? goal-id])]
    [
     ;;
     ;; place bid
     ;;
     ^{:key :place-bid}
     [ui/badge
      {:badge-content (rand-int 100)    ; FIXME: replace with bids count
       :primary true
       :badge-style {:top "15px"
               :right "20px"}}
      [ui/flat-button
        {:secondary true
         :disabled (or @my-goal?
                       @already-bidded?
                       cancelled?)
         :label "place bid"
         :on-touch-tap #(dispatch [:place-bid/show-new-bid goal-id])}]]

     ;;
     ;; cancel goal
     ;;
     ^{:key :cancel-goal}
     [ui/flat-button
      {:secondary true
       :disabled (or cancelled?
                     cancelling?
                     (not @my-goal?))
       :label "Delete"
       :on-touch-tap #(dispatch [:cancel-goal/send goal-id])}]

     ]))

;;;
;;; GOAL VIEW
;;; new version
;;;
(defn- goal-view [goal]
  (let [{:keys [goal-id
                owner
                description
                cancelled?
                cancelling?
                show-details?]} goal
        show-new-bid? (subscribe [:db/show-new-bid? goal-id])
        my-goal? (subscribe [:db/my-goal? goal-id])
        bids (subscribe [:db/sorted-bids goal-id])
        already-bidded? (subscribe [:db/already-bidded? goal-id])]

    [ui/card
     {:style (if-not cancelled?
               st/goal-card
               st/goal-card-cancelled)}

     [ui/card-header
      {:title (u/truncate description 30)
       :subtitle owner
       :show-expandable-button true
       :act-as-expander true
       :avatar (r/as-element [goal-avatar goal])}]

     [ui/card-text
      {:expandable true}
      [ui/tabs
       ;; goal
       [ui/tab
        {:label "Goal"}
        description]
       ;; bids
       [ui/tab
        {:label "Bids"}
        (if-not (empty? @bids)
          "Bids Yeah!"
          "There are no bids yet")]
       ;; details
       [ui/tab
        {:label "Details"}
        [:pre
         [:code
          (with-out-str (cljs.pprint/pprint goal))]]
        ]]

      ]

     [ui/card-actions
      {:act-as-expander false
       :children (map #(r/as-element %) (goal-actions goal))}]

     ]

    ))


;;;
;;; goals list view
;;;
(defn goals-view []
  (let [goals (subscribe [:db/sorted-goals])]
    [outer-paper
     {:style {:padding 20
              :margin-top 64}}
     [:h1 "Goals"]
     (for [goal @goals]
       ^{:key (:goal-id goal)} [goal-view goal])
     ]))
