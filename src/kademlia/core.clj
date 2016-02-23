(ns kademlia.core
  (require [aleph.udp :as udp]
           [manifold.stream :as s]
           [manifold.deferred :as d]
           [taoensso.nippy :as nippy])
  (:gen-class))

(def port 1665)
(def bind-timeout 5000) ;; milliseconds

(defn timeout [] (throw (new Exception "timeout!!!")))
(defn uuid []
  (java.util.UUID/randomUUID))

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
        parsed-message (nippy/thaw message)
        ]
    (case (:type parsed-message)
      :ping (send! socket host port {:type :ack})
      )

    )
  )

(defn bind-socket
  "Returns a deferred socket, all messages coming into the socket will be consumed by
   recv-handler. If port is 0 we will bind to an open port."
  ([] (bind-socket 0))
  ([port] (let [socket (deref (udp/socket {:port port}) bind-timeout :timeout)]
            (when (= :timeout socket) (timeout))
            (s/consume (partial #'recv-handler socket) socket)
            socket)))

(defonce socket (bind-socket port))

(defn socket->port
  "Get a port from a socket."
  [socket]
  (let [socket (:aleph/channel (meta socket))]
    (when (nil? socket)
      (throw (new Exception "socket is not a channel, maybe you need to defer it?")))
    (.getPort (.localAddress socket))))

;; TESTS

(use 'clojure.test)

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

(deftest ping-test
  (testing "when we ping do we get an ack"
    (with-stream [application-socket (bind-socket)
                  test-socket        @(udp/socket {:port 0})
                  
                  ]
      (let [application-port (socket->port application-socket)
            application-host "localhost"
            ]
        (println application-host application-port (socket->port test-socket))
        @(send! test-socket application-host application-port {:type :ping})
        (is (= {:type :ack}
               (nippy/thaw (:message @(s/take! test-socket)))))))))
