(ns childrensfutures-trade.goal-stages)

;;;
;;; ordered stages
;;;
(def sorted-stages [
                    :unknown
                    :created
                    :bid-placed
                    :bid-selected
                    :investment-sent
                    :investment-received
                    :goal-achieved
                    :bonus-asked
                    :bonus-sent
                    :goal-completed
                    :cancelled
                    ])

;;;
;;; set of stages
;;;
(def stages (set sorted-stages))

;;;
;;;
;;; human readable names of stages
;;;
;;;
(def human-readable-stages
  {:created "just added"
   :bid-selected "all set"})

(defn human-readable [stage]
  (get human-readable-stages stage stage))

;;;
;;;
;;; predicates
;;;
;;;

(defn stage? [expected-stage s]
  (= expected-stage s))

(def unknown? (partial stage? :unknown))
(def created? (partial stage? :created))
(def bid-placed? (partial stage? :bid-placed))
(def bid-selected? (partial stage? :bid-selected))
(def investment-sent? (partial stage? :investment-sent))
(def investment-received? (partial stage? :investment-received))
(def goal-achieved? (partial stage? :goal-achieved))
(def bonus-asked? (partial stage? :bonus-asked))
(def bonus-sent? (partial stage? :bonus-sent))
(def goal-completed? (partial stage? :goal-completed))
(def cancelled? (partial stage? :cancelled))

(defn after? [expected-stage s]
  (< (.indexOf sorted-stages expected-stage)
     (.indexOf sorted-stages s)))

(defn before? [expected-stage s]
  (> (.indexOf sorted-stages expected-stage)
     (.indexOf sorted-stages s)))
