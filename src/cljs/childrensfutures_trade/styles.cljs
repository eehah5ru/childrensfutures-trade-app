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
(def outer-paper-base {:padding 20
                       :margin-top 64
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

;;;
;;; GOAL OWNER AVATAR BADGE
;;;
(def goal-owner-avatar-badge {:top "20px"
                              :right "40px"
                              :background-color (color :green-200)})

;;;
;;; BID OWNER AVATAR BADGE
;;;
(def bid-owner-avatar-badge (merge goal-owner-avatar-badge
                                   {:top "-8px"
                                    :right "-5px"}))
