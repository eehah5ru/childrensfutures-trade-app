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
;;; NEW BID VIEW
;;;
;; (defn- new-bid-view [goal-id]
;;   ;; FIXME move nested code to fn!!!
;;   (let [new-bid (subscribe [:db/new-bid goal-id])]
;;     [:div
;;      [:h3 "Place Bid"]
;;      [ui/text-field {:default-value (:description @new-bid)
;;                      :on-change #(dispatch [:place-bid/update goal-id :description (u/evt-val %)])
;;                      :name "description"
;;                      :max-length 120 ;FIXME
;;                      :floating-label-text "Bid's description"
;;                      :style {:width "100%"}}]
;;      [:br]
;;      ;;
;;      ;; place
;;      ;;
;;      [ui/raised-button
;;       {:secondary false
;;        :disabled (empty? (:description @new-bid))
;;        :label "Place new bid"
;;        :on-touch-tap #(dispatch [:place-bid/send goal-id])}]
;;      ;;
;;      ;; cancel
;;      ;;
;;      [ui/flat-button
;;       {:secondary true
;;        :disabled false
;;        :label "cancel"
;;        :on-touch-tap #(dispatch [:place-bid/cancel goal-id])}]

;;      [ui/snackbar {:message "Placing bid"
;;                    :open (:placing? @new-bid)}]]))

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
         :on-touch-tap #(dispatch [:select-bid.blockchain/send goal-id bid-id])}]

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
;; (defn- goal-component1 [goal]
;;   (let [{:keys [goal-id owner description cancelled? cancelling? show-details?]} goal
;;         show-new-bid? (subscribe [:ui/show-new-bid? goal-id])
;;         my-goal? (subscribe [:db/my-goal? goal-id])
;;         bids (subscribe [:db/sorted-bids goal-id])
;;         already-bidded? (subscribe [:db/already-bidded? goal-id])]
;;     [:div {:style {:margin-top 20}
;;            :key goal-id}
;;      [:h3
;;       description]

;;      ;;
;;      ;; place bid
;;      ;;
;;      [ui/flat-button
;;       {:secondary false
;;        :disabled (or @my-goal?
;;                      @already-bidded?)
;;        :label "place bid"
;;        :on-touch-tap #(dispatch [:place-bid/show-new-bid goal-id])}]
;;      ;;
;;      ;; show details
;;      ;; FIXME: for debug only
;;      ;;
;;      [ui/flat-button
;;       {:secondary true
;;        :disabled false
;;        :label (if show-details?
;;                 "hide details"
;;                 "show details")
;;        :on-touch-tap #(dispatch [:goal/toggle-details goal-id])}]

;;      ;;
;;      ;; cancel goal
;;      ;;
;;      [ui/flat-button
;;       {:secondary true
;;        :disabled (or cancelled?
;;                      cancelling?
;;                      (not (= @(subscribe [:db/current-address]) owner)))
;;        :label "Delete"
;;        :on-touch-tap #(dispatch [:cancel-goal/send goal-id])}]


;;     ;;
;;     ;; details view
;;     ;;
;;     (when show-details?
;;       [:div.goal-details
;;        [:div {:style {:margin-top 5}}
;;         "goalId: "
;;         goal-id]
;;        [:div {:style {:margin-top 5}}
;;         "owner: "
;;         owner]])
;;     ;;
;;     ;; new bid view
;;     ;;
;;     (when @show-new-bid?
;;       (new-bid-view goal-id))

;;     (when (not (empty? @bids))
;;       [:h3 "Bids"])
;;     (for [bid @bids]
;;       ^{:key (:owner bid)} [bid-view goal-id (:owner bid) bid]) ;FIXME somehow use bid-id here

;;     [ui/divider]]))

;;;
;;;
;;; BIDS VIEWS
;;;
;;;

;;;
;;; PLACE BID
;;;
(defn- place-bid-view []
  (let [new-bid (subscribe [:db/new-bid])]
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
        new-bid (subscribe [:db/new-bid])
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
        my-bid? (subscribe [:db/my-bid? goal-id bid-id])
        my-goal? (subscribe [:db/my-goal? goal-id])
        goal-has-selected-bid? (subscribe [:db/goal-has-selected-bid? goal-id])]
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
  (let [bids (subscribe [:db/sorted-bids goal-id])]
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
        show-new-bid? (subscribe [:ui/show-new-bid? goal-id])
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
(defn goals-view [title goals-selector]
  (let [goals (goals-selector)]
    [outer-paper
     [:h1 title]
     (for [goal @goals]
       ^{:key (:goal-id goal)} [goal-view goal])
     ]))
