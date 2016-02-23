(ns kademlia.core-test
  (:require [clojure.test :refer :all]
            [aleph.udp :as udp]
            [manifold.stream :as s]
            [taoensso.nippy :as nippy]
            [kademlia.util :as util]
            [kademlia.core :refer :all]))

(deftest ping-test
  (testing "when we ping do we get an ack"
    (util/with-stream [application-socket (util/bind-socket #'recv-handler)
                       test-socket        @(udp/socket {:port 0})]
      @(send! test-socket "localhost" (util/socket->port application-socket) {:type :ping})
      (is (= {:type :ack}
             (nippy/thaw (:message @(s/take! test-socket))))))))
