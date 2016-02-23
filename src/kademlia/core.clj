(ns kademlia.core
  (:require [manifold.stream :as s]
            [taoensso.nippy :as nippy]
            [kademlia.util :as util])
  (:gen-class))

(defn send!
  "Returns a deferred that will eval to true if we successfully sent the message otherwise false."
  [socket host port msg]
  (s/put! socket
          {:host host
           :port port
           :message (nippy/freeze msg)}))

(defn recv-handler
  "Handler for all incoming messages.
  * socket: the socket receiving the message, we can use this to send a message back.
  * msg: The received message of format {:host [the host that sent the message]
                                         :port [the port that sent the message]
                                         :message [the message contents]}"
  [socket msg]
  (let [{:keys [host port message]} msg
        parsed-message              (nippy/thaw message)
        respond!                    (partial send! socket host port)]
    
    (case (:type parsed-message)
      :ping (respond! {:type :ack})
      (respond! "I don't understand what you just sent.")
      )))




(defn -main [& args]
  (util/with-stream [socket (util/bind-socket #'recv-handler)]
    (println "listening for messages on port" (util/socket->port socket))
    
    ;; We can do stuff here like initiate a conversation with another node!
    ;; TODO we probably want to broadcast a hello message or something.
    ))




