(ns childrensfutures-trade.db
  (:require [cljs-web3.core :as web3]
            [cljs.reader]
            [cljs.spec :as s]

            [childrensfutures-trade.goal-stages :as gs]))


;;;
;;;
;;; UTILS
;;;
;;;
(defn mk-web3 []
  (or (aget js/window "web3")
             (if goog.DEBUG
               (web3/create-web3 "http://localhost:8545/")
               (web3/create-web3 "https://morden.infura.io/metamask"))))

;; (defn mk-web3 []
;;   (or (aget js/window "web3")
;;       (web3/create-web3 "http://192.168.0.3:8545/")))

;;;
;;;
;;; DEFAULT VALUES
;;;
;;;

;;;
;;; default bid
;;;
(defn default-bid []
  {:created-at (js/Date.now)
   :description ""
   :goal-id nil
   :owner nil
   :placing? false
   :selected? false
   :selecting? false})

;;;
;;; default goal
;;;
(defn default-goal []
  {:created-at (js/Date.now)
   :stage :unknown
   :description ""
   :give-in-return ""
   :owner nil
   :trx-on-air? false                   ; indicates that ethereum trx is being processed
   :cancelled? false
   :bids (hash-map)
   :show-details? false})



;;;
;;;
;;; SPECS
;;;
;;;
(s/def ::stage gs/stages)

(s/def ::goal-id string?)
(s/def ::description string?)
(s/def ::give-in-return string?)
(s/def ::created-at int?)
(s/def ::indicator boolean?)
(s/def ::owner (s/or :nil nil?
                     :string string?))
(s/def ::trx-on-air? boolean?)
(s/def ::cancelled? boolean?)
(s/def ::placing? boolean?)
(s/def ::show-new-bid? boolean?)
(s/def ::show-details? boolean?)
(s/def ::show-new-goal? boolean?)
(s/def ::show-accounts? boolean?)
(s/def ::current-address string?)
(s/def ::selected? boolean?)
(s/def ::selecting? boolean?)
(s/def ::drawer-open? boolean?)

(s/def ::bid (s/keys :req-un [::created-at
                              ::goal-id
                              ::description
                              ::owner
                              ::placing?
                              ::selected?
                              ::selecting?]))

(s/def ::bids (s/map-of ::owner ::bid))

(s/def ::new-bid #(s/conform ::bid %))

(s/def ::goal (s/keys :req-un [::created-at
                               ::description
                               ::give-in-return
                               ::owner
                               ::trx-on-air?
                               ::cancelled?
                               ::bids
                               ::show-details?]))

(s/def ::new-goal #(s/conform ::goal %))

;;; goals structure
(s/def ::goals (s/map-of ::goal-id ::goal))

;;; DB structure
(s/def ::db (s/keys :req-un [::goals
                             ::new-goal
                             ::new-bid
                             ::current-address
                             ::show-new-goal?
                             ::show-new-bid?
                             ::show-accounts?
                             ::drawer-open?]))
;;;
;;;
;;; END OF SPECS
;;;
;;;


;;;
;;;
;;; STATE
;;;
;;;
(def default-db
  {:goals (hash-map)
   :settings {}                         ;FIXME: remove
   :my-addresses []
   :current-address ""
   :accounts {}
   :new-goal (default-goal)
   :new-bid (default-bid)
   :show-new-goal? false
   :show-new-bid? false
   :show-accounts? false
   :drawer-open? false
   :web3 (mk-web3)
   :provides-web3? (or (aget js/window "web3") goog.DEBUG)
   :contract {:name "GoalsStockExchange"
              :abi nil
              :bin nil
              :instance nil
              ;;
              ;;
              ;; devel net address (depends on testrpc)
              ;;
              ;;
              ;; :address "0x06db8fa0a4e2a96408c6cb82858a3cc6c9ba7ef0"

              ;;
              ;;
              ;; ropsten testnet contract address
              ;;
              ;;
              :address "0x641937c1fbf30604809e9701647af90413bb1e3a"
             }})
