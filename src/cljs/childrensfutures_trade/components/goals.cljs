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
   [childrensfutures-trade.utils :as u]
   [childrensfutures-trade.goal-stages :as gs]

   [childrensfutures-trade.components.avatars :refer [goal-avatar]]
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
   [childrensfutures-trade.components.goal.completed :as completed-goal]
   ))




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


;;;
;;; BID LIST ITEM
;;;
;; (defn- bid-list-item [goal-id bid]
;;   (let [{:keys [owner description selected?]} bid
;;         bid-id owner
;;         my-bid? (subscribe [:db.bids/my-bid? goal-id bid-id])
;;         my-goal? (subscribe [:db/my-goal? goal-id])
;;         goal-has-selected-bid? (subscribe [:db.goal/has-selected-bid? goal-id])]
;;     [ui/list-item
;;      {:left-avatar (r/as-element [bid-avatar bid])
;;       ;;:right-icon (icons/action-info)
;;       :primary-text (:description bid)
;;       :right-toggle (r/as-element
;;                      [ui/toggle
;;                       {:default-toggled selected?
;;                        :disabled (or (not @my-goal?)
;;                                      @goal-has-selected-bid?
;;                                      selected?)
;;                        :on-toggle #(dispatch [:blockchain.select-bid/send goal-id bid-id])
;;                        }])}]))

;; ;;;
;; ;;; BIDS TAB
;; ;;;
;; (defn- bids-view [goal-id]
;;   (let [bids (subscribe [:db.goal.bids/sorted goal-id])]
;;     [ui/list
;;      (for [bid @bids]
;;        ^{:key (:owner bid)}
;;        [bid-list-item goal-id bid])]))

;;;
;;;
;;; GOAL VIEW
;;;
;;;

;;;
;;; card actions
;;;
;; (defn- goal-actions [goal]
;;   (let [{:keys [goal-id show-details? cancelled? trx-on-air?]} goal
;;         my-goal? (subscribe [:db/my-goal? goal-id])
;;         already-bidded? (subscribe [:db.goal/already-bidded? goal-id])]
;;     [
;;      ;;
;;      ;; place bid
;;      ;;
;;      ^{:key :place-bid}
;;      [ui/badge
;;       {:badge-content (rand-int 100)    ; FIXME: replace with bids count
;;        :primary true
;;        :badge-style {:top "15px"
;;                :right "20px"}}
;;       [ui/flat-button
;;         {:secondary true
;;          :disabled (or @my-goal?
;;                        @already-bidded?
;;                        cancelled?)
;;          :label "place bid"
;;          :on-touch-tap #(dispatch [:place-bid/show-new-bid goal-id])}]]

;;      ;;
;;      ;; cancel goal
;;      ;;
;;      ^{:key :cancel-goal}
;;      [ui/flat-button
;;       {:secondary true
;;        :disabled (or cancelled?
;;                      trx-on-air?
;;                      (not @my-goal?))
;;        :label "Delete"
;;        :on-touch-tap #(dispatch [:cancel-goal/send goal-id])}]

;;      ]))

;;;
;;; GOAL VIEW
;;; new version
;;;
;; (defn- goal-view [goal]
;;   (let [{:keys [goal-id
;;                 owner
;;                 description
;;                 give-in-return
;;                 cancelled?
;;                 trx-on-air?
;;                 show-details?]} goal
;;         show-new-bid? (subscribe [:ui/show-new-bid? goal-id])
;;         my-goal? (subscribe [:db/my-goal? goal-id])
;;         bids (subscribe [:db.goal.bids/sorted goal-id])
;;         already-bidded? (subscribe [:db.goal/already-bidded? goal-id])]

;;     [ui/card
;;      {:style (if-not cancelled?
;;                st/goal-card
;;                st/goal-card-cancelled)}

;;      [ui/card-header
;;       {:title (r/as-element [:span
;;                 [:em "Goal "]
;;                 (u/truncate description 120)])
;;        :subtitle (r/as-element
;;                   [:span
;;                    [:em "Promises "]
;;                    give-in-return])
;;        :show-expandable-button true
;;        :act-as-expander true
;;        :avatar (r/as-element [goal-avatar goal])}]

;;      [ui/card-text
;;       {:expandable true}
;;       [ui/tabs
;;        ;; goal
;;        [ui/tab
;;         {:label "Goal"}
;;         [:h3 "Description"]
;;         description
;;         [:h3 "Promises"]
;;         give-in-return]
;;        ;; bids
;;        (when-not (empty? @bids)
;;          [ui/tab
;;           {:label "Bids"}
;;           [bids-view goal-id]])
;;        ;; details
;;        [ui/tab
;;         {:label "Details"}
;;         [:pre
;;          [:code
;;           (with-out-str (cljs.pprint/pprint goal))]]
;;         ]]

;;       ]

;;      [ui/card-actions
;;       {:act-as-expander false
;;        :children (map #(r/as-element %) (goal-actions goal))}]

;;      ]

;;     ))



(defn- goal-statuses [goal extra-statuses]
  (let [{:keys [stage goal-id]} goal
        role (subscribe [:role/role goal-id])
        extra-statuses (extra-statuses goal)]
    (concat [{:key :stage
              :content (gs/human-readable stage)}
             {:key :role
              :content @role}]
            extra-statuses)))

;;; get staged view properties
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
;;;
;;;
;;; staged goal views
;;;
;;;
(defn staged-goal-view [goal]
  (let [{:keys [goal-id stage description give-in-return]} goal
        card-properties (staged-card-properties stage)
        {:keys [card-style
                card-text
                card-actions
                card-subtitle-extra
                goal-statuses-extra]} card-properties]
    [ui/card
     {:style (card-style goal)}

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
                                [:div
                                 {:style {:display "flex"
                                          :flex-wrap "wrap"}}
                                 (for [status (goal-statuses goal goal-statuses-extra)]
                                   ^{:key (:key status)}
                                   [ui/chip
                                    {:style {:display "flex"
                                             :margin-left "4px"
                                             :margin-right "4px"}}
                                    (:content status)])]])
       :show-expandable-button true
       :act-as-expander true
       :avatar (r/as-element [goal-avatar goal])}]

     ;; text
     [ui/card-text
      {:expandable true}
      [card-text goal]]

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
(defn goals-view [goals-selector]
  (let [goals (goals-selector)]
    [:div (for [goal @goals]
            ^{:key (:goal-id goal)} [staged-goal-view goal])]
    ))
