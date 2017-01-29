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
(def main-grid {:margin-top 0})


;;;
;;;
;;; PAPER
;;;
;;;
(def paper-base {:padding-left "20px"
                 :padding-right "20px"
                 :padding-top "10px"
                 :margin-bottom 10})

(defn outer-paper-base [win-height]
  (merge paper-base
         {:margin-top "64px"
          :min-height (- win-height 128)}))


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
