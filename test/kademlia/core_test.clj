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

(deftest add-to-routing-table-test
  (testing "With an empty routing table, we add nodes to the right place"
    (let [routing-table [[] []]]
      (is (= [1 0] (map count (add-to-routing-table routing-table [true false] :node))))
      (is (= [1 0] (map count (add-to-routing-table routing-table [true true] :node))))
      (is (= [0 1] (map count (add-to-routing-table routing-table [false true] :node)))))))
