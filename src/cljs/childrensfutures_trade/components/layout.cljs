(ns childrensfutures-trade.components.layout
  (:require
    [cljs-react-material-ui.reagent :as ui]
    [childrensfutures-trade.styles :as st]
    [childrensfutures-trade.utils :as u]
    [medley.core :as medley]
    [reagent.core :as r]
    [re-frame.core :refer [dispatch subscribe]]

    ))

(def col (r/adapt-react-class js/ReactFlexboxGrid.Col))

(def row (r/adapt-react-class (aget js/ReactFlexboxGrid "Row")))

(def grid (r/adapt-react-class js/ReactFlexboxGrid.Grid))

(def outer-layout-col-flex {:xs 12 :sm 12 :md 12 :lg 6 :lg-offset 3})

(def full-width-layout-col-flex {:xs 12 :sm 12 :md 12 :lg 12})

;;;
;;;
;;; props are:
;;; - :row-props
;;; - :col-props
;;; - :paper-props
;;;
(defn- generic-paper [col-layout props-and-children]
  ;; (js/console.log props-and-children)
  (let [props (u/extract-props props-and-children)
        {:keys [row-props
                col-props
                paper-props]
         :or [row-props {}
              col-props {}
              paper-props {}]} props
        children (u/extract-children props-and-children)]
    [row row-props
     [col (r/merge-props
           col-layout
           col-props)
      [ui/paper paper-props
       (for [[index child] (medley/indexed children)]
         (with-meta child {:key index}))]]]))


(defn full-width-paper [& props-and-childern]
  (generic-paper full-width-layout-col-flex
                 (u/merge-props
                  {:col-props {:style {:margin 0
                                       :padding 0}}}
                  props-and-childern)))


(defn outer-paper [& props-and-children]
  (let [win-height (subscribe [:ui/window-height])]
    (js/console.log :debug :win-height @win-height)
    (generic-paper outer-layout-col-flex
                   (u/merge-props
                    {:paper-props {:style (st/outer-paper-base @win-height)}}
                    props-and-children))))
