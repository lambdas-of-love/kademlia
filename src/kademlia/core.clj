(ns kademlia.core
  (require [aleph.udp :as udp]
           [manifold.stream :as s]
           [taoensso.nippy :as nippy])
  (:gen-class))

;; (def socket (udp/socket {:port 12312}))

;; @(s/put! @socket {:host "localhost", :port 12312, :message (nippy/freeze {:foo :bar})})

;; (nippy/thaw (:message @(s/take! @socket)))

;; (s/close! @socket)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
