(ns childrensfutures-trade.components.layout
  (:require
    [cljs-react-material-ui.reagent :as ui]
    [childrensfutures-trade.styles :as st]
    [childrensfutures-trade.utils :as u]
    [medley.core :as medley]
    [reagent.core :as r]
    ))

(def col (r/adapt-react-class js/ReactFlexboxGrid.Col))

(def row (r/adapt-react-class (aget js/ReactFlexboxGrid "Row")))

(def grid (r/adapt-react-class js/ReactFlexboxGrid.Grid))

(def outer-layout-flex {:xs 12 :sm 12 :md 12 :lg 10 :lg-offset 1})

(defn outer-paper [& props-and-children]
  (let [row-props (u/extract-props props-and-children)
        children (u/extract-children props-and-children)]
    [row row-props
     [col outer-layout-flex
      [ui/paper {:style st/paper-base}
       (for [[index child] (medley/indexed children)]
         (with-meta child {:key index}))]]]))
