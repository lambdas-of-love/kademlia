(ns kademlia.util
  (:require [aleph.udp :as udp]
            [manifold.stream :as s]))

(defn socket->port
  "Get a port from a socket."
  [socket]
  (let [socket (:aleph/channel (meta socket))]
    (when (nil? socket)
      (throw (new Exception "socket is not a channel, maybe you need to defer it?")))
    (.getPort (.localAddress socket))))

(defmacro with-stream
  "Shamelessly modified with-open only it works on deferred streams instead."
  [bindings & body]
  (cond
    (= (count bindings) 0) `(do ~@body)
    (symbol? (bindings 0)) `(let ~(subvec bindings 0 2)
                              (try
                                (with-stream ~(subvec bindings 2) ~@body)
                                (finally
                                  (s/close! ~(bindings 0)))))
    :else (throw (IllegalArgumentException.
                   "with-stream only allows Symbols in bindings"))))

(def bind-timeout 5000) ;; milliseconds

(defn bind-socket
  "Returns a socket, all messages coming into the socket will be consumed by
   recv-handler. If port is 0 we will bind to an open port."
  ([recv-handler] (bind-socket recv-handler 0))
  ([recv-handler port] (let [socket (deref (udp/socket {:port port}) bind-timeout :timeout)]
            (when (= :timeout socket) (throw (new Exception "timed out binding socket")))
            ;; consume will send all incoming messages to the handler. We'll pass in our socket so
            ;; the handler can send messages back on the same socket in which the message was received.
            (s/consume (partial recv-handler socket) socket)
            socket)))
