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

    [childrensfutures-trade.components.goals :refer [goals-view]]))

;; (defn- goal-added-event-view [e]
;;   [:h3
;;    "Goal Added"])

;; (defn- bid-added-event-view [e]
;;   [:h3
;;    "Bid Added"])

;; (defn- bid-selected-event-view [e]
;;   [:h3
;;    "Bid Selected"])

;; (defn- unknown-event-view [e]
;;   [:h3
;;    (str "Unknown event type " (:type e))])

(defn- my-bids []
  (let [bids (subscribe [:db.bids.my/sorted])]
    [:div
     (for [bid @bids]
       (let [{:keys [owner description bid-id]} bid]
         ^{:key bid-id}
         [:h2 description]))]))

(defn- my-investments [])

(defn pulse-page []
  [outer-paper
   [ui/tabs
    [ui/tab
     {:label "My Goals"}
     [goals-view #(subscribe [:db.goals.my/sorted])]]
    [ui/tab
     {:label "Investments"}
     [:h1 "Investments"]]
    [ui/tab
     {:label "I owe"}
     [:h1 "My Bids"]
     [my-bids]]]])

;; (comment
;;   (defn pulse-page []
;;    (let [current-address (subscribe [:db/current-address])
;;          latest-events (subscribe [:db/latest-events])]
;;      [outer-paper
;;       [:h1 "Pulse"]

;;       (for [e @latest-events]
;;         (let [{:keys [key type]} e]
;;           (condp = type
;;             :goal-added ^{:key key} [goal-added-event-view e]
;;             :bid-added [bid-added-event-view e]
;;             :bid-selected [bid-selected-event-view e]
;;             :else [unknown-event-view e])))])))
