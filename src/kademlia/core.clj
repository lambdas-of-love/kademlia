(ns kademlia.core
  (:require [manifold.stream :as s]
            [taoensso.nippy :as nippy]
            [kademlia.util :as util]
            [kademlia.bitset :as bits])
  (:gen-class))

(defn send!
  "Returns a deferred that will eval to true if we successfully sent the message otherwise false."
  [socket host port msg]
  (s/put! socket
          {:host host
           :port port
           :message (nippy/freeze msg)}))

(def my-id (bits/uuid))

(def routing-table
  (atom (vec (take 128 (repeat [])))))

;; (defn add-to-routing-table
;;   "Takes a routing table (list of lists),
;;   a list of booleans representing the distance this node's id is from our id,
;;   and a node, and returns an updated routing table with the node inserted in the correct location"
;;   [routing-table distance-bit-list node]
;;   (let [first-bucket (first routing-table)
;;         first-bit (first distance-bit-list)]
;;     (if (false? first-bit)
;;       (conj (add-to-routing-table (rest routing-table) (rest distance-bit-list) node) first-bucket)
;;       (conj (rest routing-table) (conj first-bucket node)))))

(defn add-to-routing-table
  "Takes a routing table (vector of vectors),
  a list of booleans representing the distance this node's id is from our id,
  and a node, and returns an updated routing table with the node inserted in the correct location"
  [routing-table distance-bit-list node]
  (let [distance (count (take-while false? distance-bit-list))]
    (update-in routing-table [distance] (fn [list] (conj list node)))))

(defn recv-handler
  "Handler for all incoming messages.
  * socket: the socket receiving the message, we can use this to send a message back.
  * msg: The received message of format {:host [the host that sent the message]
                                         :port [the port that sent the message]
                                         :message [the message contents]}"
  [socket msg]
  (let [{:keys [host port message]} msg
        parsed-message              (try (nippy/thaw message) (catch Exception e {:type :invalid-data}))
        respond!                    (partial send! socket host port)]
    
    (case (:type parsed-message)
      :ping         (respond! {:type :ack})
      :invalid-data (respond! "hai!")
      (respond! "Invalid message type")
      )))

(defn -main [& args]
  (let [socket (util/bind-socket #'recv-handler)]
    (println "listening for messages on port" (util/socket->port socket))
    
    ;; We can do stuff here like initiate a conversation with another node!
    ;; TODO we probably want to broadcast a hello message or something.
    ))




