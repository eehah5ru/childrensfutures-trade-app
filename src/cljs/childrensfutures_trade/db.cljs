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
(defn default-pulse-event [type]
  {:number 0
   :goal-id ""
   :type type})

(defn default-pulse-goal-added []
  (default-pulse-event :goal-added))

(defn default-pulse-investment-placed []
  (default-pulse-event :investment-placed))

(defn default-pulse-investment-sent []
  (default-pulse-event :investment-sent))

(defn default-pulse-investment-received []
  (default-pulse-event :investment-received))

(defn default-pulse-goal-achieved []
  (default-pulse-event :goal-achieved))

(defn default-pulse-bonus-asked []
  (default-pulse-event :bonus-asked))

(defn default-pulse-bonus-sent []
  (default-pulse-event :bonus-sent))

(defn default-pulse-goal-completed []
  (default-pulse-event :goal-completed))

(defn default-pulse-goal-cancelled []
  (default-pulse-event :goal-cancelled))

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
   :steps-count 6})

;;;
;;;
;;; SPECS
;;;
;;;
(s/def ::stage gs/stages)
(s/def ::db-version int?)
(s/def ::db-version-synced int?)
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
(s/def ::critical-error? boolean?)
(s/def ::force-read-only? boolean?)
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
(s/def ::db-synced-at int?)
(s/def ::db-syncing? boolean?)
(s/def ::latest-block int?)

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


(s/def ::gse-contract (s/and (partial s/conform ::ethereum-contract)
                             (s/keys :req-un [::latest-block])))

(s/def ::chat-contract (s/and (partial s/conform ::ethereum-contract)
                              (s/keys :req-un [::latest-block])))

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

(s/def ::investment-placed-pulse-event
  (s/keys :req-un [::number
                   ::type
                   ::goal-id
                   ::bid-id]))

(s/def ::investment-selected-pulse-event
  (s/keys :req-un [::number
                   ::type
                   ::goal-id
                   ::bid-id]))

(s/def ::investment-sent-pulse-event
  (s/keys :req-un [::number
                   ::type
                   ::goal-id
                   ::bid-id]))

(s/def ::investment-received-pulse-event
  (s/keys :req-un [::number
                   ::type
                   ::goal-id
                   ::bid-id]))

(s/def ::goal-achieved-pulse-event
  (s/keys :req-un [::number
                   ::type
                   ::goal-id]))

(s/def ::bonus-asked-pulse-event
  (s/keys :req-un [::number
                   ::type
                   ::goal-id
                   ::bid-id]))

(s/def ::bonus-sent-pulse-event
  (s/keys :req-un [::number
                   ::type
                   ::goal-id
                   ::bid-id]))

(s/def ::goal-completed-pulse-event
  (s/keys :req-un [::number
                   ::type
                   ::goal-id]))


(s/def ::goal-cancelled-pulse-event
  (s/keys :req-un [::number
                   ::type
                   ::goal-id]))



(s/def ::pulse-event #(s/or :goal-added
                            (partial s/conform ::goal-added-pulse-event)

                            :investment-placed
                            (partial s/conform ::investment-placed-pulse-event)

                            :investment-selected
                            (partial s/conform ::investment-selected-pulse-event)

                            :investment-sent
                            (partial s/conform ::investment-sent-pulse-event)

                            :investment-received
                            (partial s/conform ::investment-received-pulse-event)

                            :goal-achieved
                            (partial s/conform ::goal-achieved-pulse-event)

                            :bonus-asked
                            (partial s/conform ::bonus-asked-pulse-event)

                            :bonus-sent
                            (partial s/conform ::bonus-sent-pulse-event)

                            :goal-completed
                            (partial s/conform ::goal-completed-pulse-event)

                            :goal-cancelled
                            (partial s/conform ::goal-cancelled-pulse-event)))
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
                             ::db-version-synced
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
                             ::critical-error?
                             ::force-read-only?
                             ::show-new-goal?
                             ::show-new-bid?
                             ::show-accounts?
                             ::drawer-open?
                             ::chat-open?
                             ::window-height
                             ::db-synced-at
                             ::db-syncing?
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
   :force-read-only? false
   :critical-error? false
   :show-new-goal? false
   :show-new-bid? false
   :show-accounts? false
   :drawer-open? false
   :chat-open? false
   :window-height nil
   :web3 (mk-web3)
   :provides-web3? (provides-web3?)
   :db-synced-at 0
   :db-syncing? false
   :db-version-synced 0
   ;;
   ;; gse contract
   ;;
   :gse-contract (cond-> {:name "GoalsStockExchange"
                          :abi nil
                          :bin nil
                          :instance nil
                          :latest-block 0
                          }

                   ;; ropsten testnet in production
                   (= "production" childrensfutures-trade.utils/CONTRACTS)
                   (merge {:address "0x4febdfbf70c28ae01dcdbfb490883a31499ed6c2"
                           :from-block 497069})


                   ;; ropsten testnet in staging
                   (= "staging" childrensfutures-trade.utils/CONTRACTS)
                   (merge {:address "0xe1059f8c273776fea9d4fb7de1b14fb4f684208d"
                           :from-block 868341})

                   ;; ropsten testnet in devel env
                   ;; childrensfutures-trade.utils/DEV
                   ;; (merge {:address "0x4febdfbf70c28ae01dcdbfb490883a31499ed6c2"
                   ;;         :from-block 497069})

                   ;; devel GSE contract address
                   ;; depends on testrpc
                   ;; childrensfutures-trade.utils/DEV
                   ;; (merge {:address "0x2d30b9315448ddd146cb17e606ab594f3eccf910"
                   ;;         :from-block 0})
                   )

   ;;
   ;; chat contract
   ;;
   :chat-contract (cond-> {:name "Chat"
                           :abi nil
                           :bin nil
                           :instance nil
                           :latest-block 0}

                    ;;
                    ;; production contract address
                    ;;
                   (= "production" childrensfutures-trade.utils/CONTRACTS)
                    (merge {:address "0x6e600f0939c3aec1aece8735c38f2e10ccc44cf3"
                            :from-block 487332})

                    ;;
                    ;; production contract address
                    ;;
                   (= "staging" childrensfutures-trade.utils/CONTRACTS)
                    (merge {:address "0x777117851865397d4659d84f1ce3b122bd1a0819"
                            :from-block 868350})


                    ;;
                    ;; devel Chat contract address
                    ;;
                    ;; in ropsten
                    ;; childrensfutures-trade.utils/DEV
                    ;; (merge {:address "0x6e600f0939c3aec1aece8735c38f2e10ccc44cf3"
                    ;;         :from-block 487332})

                    ;; depends on testrpc
                    ;; childrensfutures-trade.utils/DEV
                    ;; (merge {:address "0x7759a1442466ea622e44238de2e628b2001b8741"
                    ;;         :from-block 0})

                    )
   })


;;;
;;;
;;; data access utils
;;;
;;;

;;; get goal or default

(defn get-goal [db goal-id]
  (-> db
      (get-in [:goals goal-id] (default-goal))
      (assoc :goal-id goal-id)))

;;; get bid or default
(defn get-bid [db goal-id bid-id]
  (-> db
      (get-goal goal-id)
      (get-in [:bids bid-id] (default-bid))
      (assoc :bid-id bid-id)))

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
