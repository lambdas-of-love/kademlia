(ns kademlia.core-test
  (:require [clojure.test :refer :all]
            [aleph.udp :as udp]
            [manifold.stream :as s]
            [taoensso.nippy :as nippy]
            [kademlia.util :as util]
            [kademlia.core :refer :all]))

(def packet-response-timeout 500) ;; milliseconds

(defn no-response? [stream]
  (= :timeout
     (deref (s/take! stream)
            packet-response-timeout
            :timeout)))

(defn test-send!
  "Given a test socket, and a socket that has been bound with a recv handler
   send a message from the test socket to the socket with the handler"
  [test-socket
   socket-with-recv-handler
   message]
  @(send! test-socket
          "localhost"
          (util/socket->port socket-with-recv-handler)
          message))

(deftest ping-test
  (testing "do we timeout when binding to a port we shouldn't be able to bind to?"
    (is (thrown? java.util.concurrent.TimeoutException
                 (util/bind-socket #'recv-handler 1))))
  
  (testing "when we ping do we get an ack?"
    (with-open [application-socket (util/bind-socket #'recv-handler)
                test-socket        @(udp/socket {:port 0})]
      
      (test-send! test-socket application-socket {:type :ping})
      
      (is (= {:type :ack}
             (nippy/thaw (:message @(s/take! test-socket)))))))

  (testing "when we send an unknown message type do we get no response?"
    (with-open [application-socket (util/bind-socket #'recv-handler)
                test-socket        @(udp/socket {:port 0})]

      (test-send! test-socket application-socket {:type :NOTAREALMESSAGETYPE})

      (is (no-response? test-socket))))
  
  (testing "when we send a message without a :type key do we explode?"
    (with-open [application-socket (util/bind-socket #'recv-handler)
                test-socket        @(udp/socket {:port 0})]

      (test-send! test-socket application-socket {})

      (is (no-response? test-socket))))
  
  (testing "when we send non valid data in the message field do we get no response?"
    (with-open [application-socket (util/bind-socket #'recv-handler)
                test-socket        @(udp/socket {:port 0})]
      
      @(s/put! test-socket {:host "localhost"
                            :port (util/socket->port application-socket)
                            :message "I'm not valid serialized data! :D"})
      
      (is (no-response? test-socket)))))



