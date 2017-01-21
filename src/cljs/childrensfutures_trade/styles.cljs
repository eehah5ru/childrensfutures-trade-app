(ns childrensfutures-trade.styles
  (:require
   [cljs-react-material-ui.core :refer [color]]))

;;;
;;;
;;; COLORS
;;;
;;;
(def color-disabled (color :grey-200))

;;;
;;;
;;; GRID
;;;
;;;
(def main-grid {:margin-top 20})


;;;
;;;
;;; PAPER
;;;
;;;
(def paper-base {:padding 20
                 :margin-top 10
                 :margin-bottom 10})


;;;
;;;
;;; GOAL VIEW
;;;
;;;
(def goal-card {:position "relative"
                :margin-top 20
                :margin-bottom 20})

(def goal-card-cancelled (merge goal-card
                                {:background-color color-disabled}))
