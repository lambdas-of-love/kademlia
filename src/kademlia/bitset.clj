(ns kademlia.bitset)

(def empty-bitset
  (java.util.BitSet/valueOf (long-array [0 0])))

(defn uuid []
  (java.util.UUID/randomUUID))

(defn uuid->bitset
  "Convert a UUID to a BitSet of 128 bits."
  [uuid]
  (java.util.BitSet/valueOf (long-array [(.getMostSignificantBits uuid)
                                         (.getLeastSignificantBits uuid)])))

(defn bitset->uuid
  "Converts a BitSet to a UUID. Bitset must be 128 bits."
  [bitset]
  (let [longs (.toLongArray bitset)]
    (assert (= 2 (count longs)))
    (new java.util.UUID (aget longs 0) (aget longs 1))))

(defn bitset->byte-array
  "Converts a BitSet to a Byte array."
  [bitset]
  (.toByteArray bitset))

(defn byte-array->bitset
  "Converts a Byte array to a BitSet."
  [byte-array]
  (java.util.BitSet/valueOf byte-array))

(defn xor
  "XOR two bitsets returning a new bitset"
  [a b]
  ;; the BitSet .xor method mutates the original bitset.
  (let [a (.clone a)]
    (.xor a b)
    a))

(defn cardinality
  "The number of 1's in a BitSet."
  [bitset]
  (.cardinality bitset))

(defn bitset->list [bitset]
  (for [i (range (.length bitset))]
    (.get bitset i)))
