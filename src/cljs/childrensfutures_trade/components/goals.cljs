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
;;;
;;; AVATARS
;;;
;;;

;;;
;;; goal avatar
;;;
(defn- goal-avatar [goal]
  (let [goals-count (subscribe [:db.goals/count (:owner goal)])]
    [ui/badge
     {:badge-content @goals-count
      :badge-style st/goal-owner-avatar-badge}
     [ui/avatar {:style {:margin-top 5
                         :margin-right 20}
                 :src (clavatar/gravatar (:owner goal))}]]))

;;;
;;; bid avatar
;;;
(defn- bid-avatar [bid]
  (let [goals-count (subscribe [:db.goals/count (:owner bid)])]
    [:div
     {:style {:position "absolute"
              :left "15px"
              :transform "translateY(-50%)"
              :top "50%"}}
     [ui/badge
      {:badge-content @goals-count
       :badge-style st/bid-owner-avatar-badge
       :style {:display "block"
               :padding 0
               :position "relative"
               :height 40
               :width 40}}
      [ui/avatar {:style {;;:margin-left "15px"
                          ;; :position "absolute"
                          ;; :margin-left "-60px"
                          ;; :top "50%"
                          ;;:transform "translateY(-50%)"
                          }
                  :src (clavatar/gravatar (:owner bid))}]]]))


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
                     :max-length 2000 ;FIXME
                     :floating-label-text "Bid's description"
                     :style {:width "100%"}}]]))


(defn place-bid-dialog []
  (let [show-new-bid? (subscribe [:ui/show-new-bid?])
        new-bid (subscribe [:db.bids/new-bid])
        place-button [ui/raised-button
                      {:secondary true
                       :disabled (empty? (:description @new-bid))
                       :label "Place"
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


;;;
;;; BID LIST ITEM
;;;
(defn- bid-list-item [goal-id bid]
  (let [{:keys [owner description selected?]} bid
        bid-id owner
        my-bid? (subscribe [:db.bids/my-bid? goal-id bid-id])
        my-goal? (subscribe [:db/my-goal? goal-id])
        goal-has-selected-bid? (subscribe [:db.goal/has-selected-bid? goal-id])]
    [ui/list-item
     {:left-avatar (r/as-element [bid-avatar bid])
      ;;:right-icon (icons/action-info)
      :primary-text (:description bid)
      :right-toggle (r/as-element
                     [ui/toggle
                      {:default-toggled selected?
                       :disabled (or (not @my-goal?)
                                     @goal-has-selected-bid?
                                     selected?)
                       :on-toggle #(dispatch [:select-bid.blockchain/send goal-id bid-id])
                       }])}]))

;;;
;;; BIDS TAB
;;;
(defn- bids-view [goal-id]
  (let [bids (subscribe [:db.goal.bids/sorted goal-id])]
    [ui/list
     (for [bid @bids]
       ^{:key (:owner bid)}
       [bid-list-item goal-id bid])]))

;;;
;;;
;;; GOAL VIEW
;;;
;;;

;;;
;;; card actions
;;;
(defn- goal-actions [goal]
  (let [{:keys [goal-id show-details? cancelled? cancelling?]} goal
        my-goal? (subscribe [:db/my-goal? goal-id])
        already-bidded? (subscribe [:db.goal/already-bidded? goal-id])]
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
                give-in-return
                cancelled?
                cancelling?
                show-details?]} goal
        show-new-bid? (subscribe [:ui/show-new-bid? goal-id])
        my-goal? (subscribe [:db/my-goal? goal-id])
        bids (subscribe [:db.goal.bids/sorted goal-id])
        already-bidded? (subscribe [:db.goal/already-bidded? goal-id])]

    [ui/card
     {:style (if-not cancelled?
               st/goal-card
               st/goal-card-cancelled)}

     [ui/card-header
      {:title (r/as-element [:span
                [:em "Goal "]
                (u/truncate description 120)])
       :subtitle (r/as-element
                  [:span
                   [:em "Promises "]
                   give-in-return])
       :show-expandable-button true
       :act-as-expander true
       :avatar (r/as-element [goal-avatar goal])}]

     [ui/card-text
      {:expandable true}
      [ui/tabs
       ;; goal
       [ui/tab
        {:label "Goal"}
        [:h3 "Description"]
        description
        [:h3 "Promises"]
        give-in-return]
       ;; bids
       (when-not (empty? @bids)
         [ui/tab
          {:label "Bids"}
          [bids-view goal-id]])
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
(defn goals-view [goals-selector]
  (let [goals (goals-selector)]
    [:div (for [goal @goals]
       ^{:key (:goal-id goal)} [goal-view goal])]
    ))
