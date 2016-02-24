(ns kademlia.bitset-test
  (:require [kademlia.bitset :refer :all]
            [clojure.test :refer :all]))

(deftest test-bitset
  (testing "there and back again, uuid bitset conversion"
    (let [u (uuid)]
      (is (= u (bitset->uuid (uuid->bitset u)))))))
