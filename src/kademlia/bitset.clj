(ns kademlia.bitset)

(defn uuid []
  (java.util.UUID/randomUUID))

(defn uuid->bitset [uuid]
  (java.util.BitSet/valueOf (long-array [(.getMostSignificantBits uuid)
                                         (.getLeastSignificantBits uuid)])))

(defn bitset->uuid
  "Converts a bitset to a UUID. Bitset must be 128 bits."
  [bitset]
  (let [longs (.toLongArray bitset)]
    (assert (= 2 (count longs)))
    (new java.util.UUID (aget longs 0) (aget longs 1))))


(defn xor
  "XOR two bitsets returning a new bitset"
  [a b]
  ;; the BitSet .xor method mutates the original bitset.
  (let [a (.clone a)]
    (.xor a b)
    a))
