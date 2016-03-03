(ns kademlia.core
  (:require [manifold.stream :as s]
            [taoensso.nippy :as nippy]
            [kademlia.util :as util])
  (:gen-class))

(def do-nothing nil)

(defn send!
  "Returns a deferred that will eval to true if we successfully sent the message otherwise false."
  [socket host port msg]
  (s/put! socket
          {:host host
           :port port
           :message (nippy/freeze msg)}))


(defn attempt-to-thaw-msg
  "Returns the parsed serialized data, if we failed to parse it return nil.:
  {:type :invalid-data}"
  [msg]
  (try
    (nippy/thaw msg)
    (catch Exception e
      nil)))

(defn recv-handler
  "Handler for all incoming messages.
  * socket: the socket receiving the message, we can use this to send a message back.
  * msg: The received message of format {:host [the host that sent the message]
                                         :port [the port that sent the message]
                                         :message [the message contents]}"
  [socket msg]
  (let [{:keys [host port message]} msg
        respond!                    (partial send! socket host port)]

    ;; If we fail parsing the message do nothing.
    (when-let [parsed-message (attempt-to-thaw-msg (:message msg))]

      (case (:type parsed-message)
        :ping (respond! {:type :ack})
        ;; TODO other message types here.
        
        ;; Do nothing if we don't understand the message.
        nil))))

(defn -main [& args]
  (let [socket (util/bind-socket #'recv-handler)]
    (println "listening for messages on port" (util/socket->port socket))
    
    ;; We can do stuff here like initiate a conversation with another node!
    ;; TODO we probably want to broadcast a hello message or something.
    ))




