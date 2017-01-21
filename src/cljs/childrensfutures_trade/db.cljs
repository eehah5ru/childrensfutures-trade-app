(ns childrensfutures-trade.db
  (:require [cljs-web3.core :as web3]
            [cljs.reader]
            [cljs.spec :as s]))


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
   :owner nil
   :placing? false
   :selected? false
   :selecting? false})

;;;
;;; default goal
;;;
(defn default-goal []
  {:created-at (js/Date.now)
   :description ""
   :owner nil
   :sending? false
   :cancelling? false
   :cancelled? false
   :bids (hash-map)
   :show-details? false})



;;;
;;;
;;; SPECS
;;;
;;;
(s/def ::goal-id string?)
(s/def ::description string?)
(s/def ::created-at int?)
(s/def ::indicator boolean?)
(s/def ::owner (s/or :nil nil?
                     :string string?))
(s/def ::sending? boolean?)
(s/def ::cancelling boolean?)
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
                               ::owner
                               ::sending?
                               ::cancelling?
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
              :address "0x6dc29e701c4bbc33f89baba91f37b0ade58b7987"}})
