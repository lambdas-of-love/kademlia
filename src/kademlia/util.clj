(ns kademlia.util
  (:require [aleph.udp :as udp]
            [manifold.stream :as s]))

(defn socket->port
  "Get a port from a socket."
  [socket]
  (let [socket (:aleph/channel (meta socket))]
    (.getPort (.localAddress socket))))

(def bind-timeout 1000) ;; milliseconds

(defn bind-socket
  "Returns a socket, all messages coming into the socket will be consumed by
   recv-handler. If port is 0 we will bind to an open port."

  ;; Attempt to bind to an available port.
  ([recv-handler]
   (bind-socket recv-handler 0))

  ;; Allow the user to specify a specific port to bind on.
  ([recv-handler port]
   (let [socket (deref (udp/socket {:port port})
                       bind-timeout
                       :timeout)]
     
     (when (= :timeout socket)
       (throw (new java.util.concurrent.TimeoutException
                   (format "timed out binding to port %s" port))))
     
     ;; consume will send all incoming messages to the handler. We'll pass in our socket so
     ;; the handler can send messages back on the same socket in which the message was received.
     (s/consume (partial recv-handler socket) socket)

     ;; Return the socket.
     socket)))
