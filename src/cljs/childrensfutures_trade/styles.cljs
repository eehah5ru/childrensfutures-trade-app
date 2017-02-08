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
(defn main-grid [win-height]
  {:margin-top 0
   :padding-top 44
   :min-height (- win-height 108)})


;;;
;;;
;;; PAPER
;;;
;;;
(def paper-base {:padding-left "20px"
                 :padding-right "20px"
                 :padding-top "10px"
                 :padding-bottom "20px"})

(defn outer-paper-base [win-height]
  (merge paper-base
         {:margin-top "20px"}))



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
;;; card button
;;;
(def goal-card-button {:margin-left "20px"
                       :margin-bottom "20px"})

;;;
;;; GOAL OWNER AVATAR BADGE
;;;
(def goal-owner-avatar-badge {:top "20px"
                              :right "40px"
                              :background-color (color :indigo-50)})

;;;
;;; BID OWNER AVATAR BADGE
;;;
(def bid-owner-avatar-badge (merge goal-owner-avatar-badge
                                   {:top "-8px"
                                    :right "-5px"}))
