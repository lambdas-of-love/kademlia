(ns kademlia.core-test
  (:require [clojure.test :refer :all]
            [aleph.udp :as udp]
            [manifold.stream :as s]
            [taoensso.nippy :as nippy]
            [kademlia.util :as util]
            [kademlia.core :refer :all]))

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
             (nippy/thaw (:message @(s/take! test-socket))))))))



