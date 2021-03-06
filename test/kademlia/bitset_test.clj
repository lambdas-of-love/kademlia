(ns kademlia.bitset-test
  (:require [kademlia.bitset :refer :all]
            [clojure.test :refer :all]))

(deftest test-bitset
  (testing "there and back again, uuid BitSet conversion"
    (let [u (uuid)]
      (is (= u (bitset->uuid (uuid->bitset u))))))

  (testing "there and back again, BitSet and Byte array conversion"
    (let [bitset (uuid->bitset (uuid))]
      (is (= bitset (byte-array->bitset (bitset->byte-array bitset))))))
  
  (testing "cardinality"
    (is (= 0 (cardinality empty-bitset))))
  
  (testing "xor"
    (let [a (uuid->bitset (uuid))
          b (uuid->bitset (uuid))]
      (is (not= a b))
      (is (= empty-bitset (xor a a)))
      (is (not= empty-bitset (xor a b)))
      (is (not= a (xor a b)))
      (is (not= b (xor a b)))))

  (testing "set-bit"
    (is (= 1 (cardinality (set-bit empty-bitset 0 true)))))
  
  (testing "bitset->list"
    (is (= (take 128 (repeat false))
           (bitset->list empty-bitset)))
    (is (= (concat [true] (take 127 (repeat false)))
           (bitset->list (set-bit empty-bitset 0 true))))))
