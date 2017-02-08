(ns childrensfutures-trade.components.home-page
  (:require
    [cljs-react-material-ui.icons :as icons]
    [cljs-react-material-ui.reagent :as ui]
    [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
    [childrensfutures-trade.styles :as st]
    [medley.core :as medley]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]

    [childrensfutures-trade.components.goals :refer [goals-view]]
    [childrensfutures-trade.components.goal.common :refer [no-goals-view]]))


(defn ^:export home-page []
  [outer-paper
   (goals-view
    #(subscribe [:db.goals/sorted])
    :no-goals-view no-goals-view)]

  )
