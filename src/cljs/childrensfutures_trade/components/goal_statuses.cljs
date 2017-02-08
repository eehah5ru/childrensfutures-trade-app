(ns childrensfutures-trade.components.goal-statuses
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

   ))

;;;
;;; render statuses
;;;
(defn render [statuses]
  [:div
   {:style {:display "flex"
            :flex-wrap "wrap"}}
   (for [status statuses]
     ^{:key (:key status)}
     [ui/chip
      {:style {:display "flex"
               :margin-left "4px"
               :margin-right "4px"}}
      (:content status)])])

;;;
;;; generic goal statuses
;;;
(defn goal-statuses [goal extra-statuses]
  (let [{:keys [stage goal-id]} goal
        role (subscribe [:role/role goal-id])
        stranger? (subscribe [:role/stranger? goal-id])
        extra-statuses (extra-statuses goal)]
    (concat (cond-> [{:key :stage
                      :content (gs/human-readable stage)}]
              (not @stranger?)
              (conj {:key :role
                     :content (get {:goal-owner "goal owner"
                                    :bid-owner "bid owner"}
                                   @role
                                   @role)}))
            extra-statuses)))

;;;
;;;
;;; STAGED
;;;
;;;
(defn- bid-placed-chips [goal]
  (let [goal-id (:goal-id goal)
        bids (subscribe [:db.goal.bids/sorted goal-id])
        content (str (count @bids) " potential investor")
        content (if (= (count @bids) 1)
                  content
                  (str content "s"))]
    [{:key :investors-count
      :content content}]))


;;;
;;; staged goal statuses
;;;
(defn staged-goal-statuses [goal]
  (let [stage (:stage goal)]
    (cond-> []
      (gs/stage? :bid-placed stage) (conj bid-placed-chips)
      true (as-> staged-statuses-fns
               (goal-statuses goal
                              (fn [g] (mapcat #(% g) staged-statuses-fns)))))))
