(ns childrensfutures-trade.db
  (:require [cljs-web3.core :as web3]
            [cljs.reader]
            [cljs.spec :as s]

            [childrensfutures-trade.goal-stages :as gs]

            [childrensfutures-trade.utils :as u]))


;;;
;;;
;;; UTILS
;;;
;;;
(defn provides-web3? []
  (not (nil? (aget js/window "web3"))))

(defn mk-web3 []
  (aget js/window "web3"))

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
  {:created-at 0
   :stage :unknown
   :description ""
   :give-in-return ""
   :owner nil
   :show-share-url? false
   :trx-on-air? false                   ; indicates that ethereum trx is being processed
   :cancelled? false
   :bids (hash-map)
   :show-details? false})

;;;
;;; default chat channel id
;;;
(def default-chat-channel-id
  (if (provides-web3?)
    (web3/sha3 "myfutures.trade")
    "myfutures.trade"))

;;;
;;; default chat message
;;;
(defn default-chat-message []
  {:channel-id default-chat-channel-id
   :message-id nil
   :text ""
   :owner nil
   :trx-on-air? false})

;;;
;;;
;;; default pulse states
;;;
;;;
(defn default-pulse-goal-added []
  {:number 0
   :goal-id ""
   :type :goal-added})

(defn default-pulse-investment-placed []
  {:number 0
   :type :investment-placed
   :goal-id ""
   :bid-id ""})

(defn default-pulse []
  [])

;;;
;;; default aux structure for conforming bid selection
;;;
(defn default-select-bid []
  {:goal-id ""
   :bid-id ""
   :dialog-open? false})

;;;
;;; default aux structure for viewing a goal
;;;
(defn default-view-goal []
  {:goal-id ""
   :dialog-open? false
   :on-view-goal-page? false})

;;;
;;; default aux structure for how to play page
;;;
(defn default-how-to-play []
  {:step 0
   :steps-count 5})

;;;
;;;
;;; SPECS
;;;
;;;
(s/def ::stage gs/stages)
(s/def ::db-version int?)
(s/def ::goal-id string?)
(s/def ::channel-id string?)
(s/def ::message-id int?)
(s/def ::text string?)
(s/def ::description string?)
(s/def ::give-in-return string?)
(s/def ::created-at int?)
(s/def ::step int?)
(s/def ::steps-count int?)
(s/def ::indicator boolean?)
(s/def ::owner (s/or :nil nil?
                     :string string?))
(s/def ::trx-on-air? boolean?)
(s/def ::cancelled? boolean?)
(s/def ::placing? boolean?)
(s/def ::show-new-bid? boolean?)
(s/def ::show-details? boolean?)
(s/def ::show-new-goal? boolean?)
(s/def ::show-share-url? boolean?)
(s/def ::show-accounts? boolean?)
(s/def ::current-address string?)
(s/def ::current-chat-channel-id string?)
(s/def ::selected? boolean?)
(s/def ::selecting? boolean?)
(s/def ::drawer-open? boolean?)
(s/def ::dialog-open? boolean?)
(s/def ::on-view-goal-page? boolean?)
(s/def ::chat-open? boolean?)
(s/def ::type #{:goal-added
                :investment-placed})
(s/def ::number int?)

(s/def ::window-height (s/or :nil nil?
                             :integer integer?))

;;;
;;; contracts
;;;
(s/def ::address string?)
(s/def ::name string?)
(s/def ::ethereum-contract (s/keys :req-un [::address
                                            ::name]))


(s/def ::gse-contract (partial s/conform ::ethereum-contract))
(s/def ::chat-contract (partial s/conform ::ethereum-contract))

;;;
;;; structures
;;;

(s/def ::bid (s/keys :req-un [::created-at
                              ::goal-id
                              ::description
                              ::owner
                              ::placing?
                              ::selected?
                              ::selecting?]))

;;;
;;; selct bids
;;;
(s/def ::select-bid (s/keys :req-un [::goal-id
                                     ::bid-id
                                     ::dialog-open?]))

;;;
;;; view goal dialog
;;;
(s/def ::view-goal (s/keys :req-un [::goal-id
                                    ::dialog-open?
                                    ::on-view-goal-page?]))

;;;
;;; how to play
;;;
(s/def ::how-to-play (s/keys :req-un [::step
                                      ::steps-count]))

;;;
;;; chat message
;;;
(s/def ::chat-message (s/keys :req-un [::channel-id
                                       ::message-id
                                       ::owner
                                       ::text
                                       ::trx-on-air?]))


(s/def ::new-bid #(s/conform ::bid %))

(s/def ::new-chat-message (partial s/conform ::chat-message))

(s/def ::goal (s/keys :req-un [::created-at
                               ::description
                               ::give-in-return
                               ::owner
                               ::trx-on-air?
                               ::cancelled?
                               ::bids
                               ::show-details?]))

(s/def ::new-goal #(s/conform ::goal %))

;;;
;;; pulse structures
;;;
(s/def ::goal-added-pulse-event
  (s/keys :req-un [::number
                   ::type
                   ::goal-id]))

(s/def ::investmentplaced-pulse-event
  (s/keys :req-un [::number
                   ::type
                   ::goal-id
                   ::bid-id]))

(s/def ::pulse-event #(s/or :goal-added
                            (partial s/conform ::goal-added-pulse-event)
                            :investment-placed
                            (partial s/conform ::investmentplaced-pulse-event)))
;;;
;;; collections
;;;
(s/def ::messages (s/map-of ::channel-id (s/coll-of ::chat-message)))

(s/def ::chat-threads (s/map-of ::owner (s/coll-of ::channel-id)))

(s/def ::pulse (s/coll-of ::pulse-event))

(s/def ::bids (s/map-of ::owner ::bid))

;;; goals structure
(s/def ::goals (s/map-of ::goal-id ::goal))

;;; DB structure
(s/def ::db (s/keys :req-un [::db-version
                             ::goals
                             ;; ::messages
                             ;; ::pulse
                             ;; ::view-goal
                             ::chat-threads
                             ::how-to-play
                             ::new-goal
                             ::new-bid
                             ::new-chat-message
                             ::select-bid
                             ::current-address
                             ::current-chat-channel-id
                             ::show-new-goal?
                             ::show-new-bid?
                             ::show-accounts?
                             ::drawer-open?
                             ::chat-open?
                             ::window-height
                             ::gse-contract
                             ::chat-contract]))
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
  {:db-version 0
   :goals (hash-map)
   :messages (hash-map)
   :chat-threads (hash-map)
   :pulse (default-pulse)
   :settings {}                         ;FIXME: remove
   :my-addresses []
   :current-address ""
   :current-chat-channel-id default-chat-channel-id
   :accounts {}
   :new-goal (default-goal)
   :new-bid (default-bid)
   :new-chat-message (default-chat-message)
   :select-bid (default-select-bid)
   :view-goal (default-view-goal)
   :how-to-play (default-how-to-play)
   :show-new-goal? false
   :show-new-bid? false
   :show-accounts? false
   :drawer-open? false
   :chat-open? false
   :window-height nil
   :web3 (mk-web3)
   :provides-web3? (not (nil? (aget js/window "web3")))
   ;;
   ;; gse contract
   ;;
   :gse-contract (cond-> {:name "GoalsStockExchange"
                          :abi nil
                          :bin nil
                          :instance nil
                          ;; ropsten testnet contract address
                          :address "0x641937c1fbf30604809e9701647af90413bb1e3a"
                          }
                   childrensfutures-trade.utils/DEV
                   ;; devel GSE contract address
                   ;; depends on testrpc
                   (merge {:address "0x2f4225883ac9d7e816ed99dc2f3149857a985859"}))
   ;;
   ;; chat contract
   ;;
   :chat-contract (cond-> {:name "Chat"
                           ;; ropsten testnet contract address
                           :address "0x6e600f0939c3aec1aece8735c38f2e10ccc44cf3"}
                    childrensfutures-trade.utils/DEV
                    ;; devel Chat contract address
                    ;; depends on testrpc
                    (merge {:address "0x7759a1442466ea622e44238de2e628b2001b8741"}))})


;;;
;;;
;;; data access utils
;;;
;;;

;;; get goal or default

(defn get-goal [db goal-id]
  (get-in db [:goals goal-id] (default-goal)))

;;; get bid or default
(defn get-bid [db goal-id bid-id]
  (-> db
      (get-goal goal-id)
      (get-in [:bids bid-id] (default-bid))))

;;; returns updated db
(defn update-goal [db goal-id f]
  (-> db
      (get-goal goal-id)
      (f)
      (as-> g (assoc-in db [:goals goal-id] g))))

;;; returns updated db
(defn change-stage [db goal-id new-stage]
  (let [goal (get-goal db goal-id)]
    (cond-> db
      (gs/after? (:stage goal) new-stage)
      (update-goal goal-id #(assoc % :stage new-stage)))))

;;; returns updated db
(defn update-bid [db goal-id bid-id f]
  (-> db
      (get-bid goal-id bid-id)
      (f)
      (as-> b (assoc-in db [:goals goal-id :bids bid-id] b))))
