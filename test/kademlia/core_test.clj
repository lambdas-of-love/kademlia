(ns kademlia.core-test
  (:require [clojure.test :refer :all]
            [aleph.udp :as udp]
            [manifold.stream :as s]
            [taoensso.nippy :as nippy]
            [kademlia.util :as util]
            [kademlia.core :refer :all]))

(def packet-response-timeout 500) ;; milliseconds

(deftest ping-test
  (testing "do we timeout when binding to a port we shouldn't be able to bind to?"
    (is (thrown? java.util.concurrent.TimeoutException
                 (util/bind-socket #'recv-handler 1))))
  
  (testing "when we ping do we get an ack"
    (with-open [application-socket (util/bind-socket #'recv-handler)
                test-socket        @(udp/socket {:port 0})]
      @(send! test-socket
              "localhost"
              (util/socket->port application-socket) {:type :ping})
      (is (= {:type :ack}
             (nippy/thaw (:message @(s/take! test-socket)))))))

  (testing "when we send an unknown message type do we get no response?"
    (with-open [application-socket (util/bind-socket #'recv-handler)
                test-socket        @(udp/socket {:port 0})]
      @(send! test-socket
              "localhost"
              (util/socket->port application-socket) {:type :NOTAREALMESSAGETYPE})
      (is (= :timeout
             (deref (s/take! test-socket)
                    packet-response-timeout
                    :timeout)))))
  (testing "when we send a message without a :type key do we explode?"
    (with-open [application-socket (util/bind-socket #'recv-handler)
                test-socket        @(udp/socket {:port 0})]
      @(send! test-socket
              "localhost"
              (util/socket->port application-socket) {})
      (is (= :timeout
             (deref (s/take! test-socket)
                    packet-response-timeout
                    :timeout)))))
  
  (testing "when we send non valid data in the message field do we get no response?"
    (with-open [application-socket (util/bind-socket #'recv-handler)
                test-socket        @(udp/socket {:port 0})]
      @(s/put! test-socket {:host "localhost"
                           :port (util/socket->port application-socket)
                           :message "I'm not valid serialized data! :D"})
      (is (= :timeout
             (deref (s/take! test-socket)
                    packet-response-timeout
                    :timeout))))))



