(ns childrensfutures-trade.components.my-investments-page
  (:require
    [cljs-react-material-ui.icons :as icons]
    [cljs-react-material-ui.reagent :as ui]
    [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
    [childrensfutures-trade.styles :as st]
    [childrensfutures-trade.utils :as u]
    [medley.core :as medley]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]

    [childrensfutures-trade.components.goals :refer [goals-view]]
    [childrensfutures-trade.components.goal.common :refer [no-goals-view
                                                           no-investments-view]]
    ))


(defn ^:export my-investments-page []
  [outer-paper
   (goals-view #(subscribe [:db.goals.my-investments/sorted])
               :no-goals-view no-investments-view)]

  )
