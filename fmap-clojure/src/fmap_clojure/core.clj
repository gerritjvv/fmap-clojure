(ns fmap-clojure.core
  (:require [fun-utils.core :refer [star-channel]])
  )


(defrecord SharedIO [ k io ])

(defn IO [ io & {:keys [k] :or {k (hash io)}}]
  "Creates a SharedIO instance from io and if k is not defined uses the (hash io) as the key"
  (SharedIO. k io))

(def shared-io-star-chan (star-channel))

(defprotocol Functor
     (fmap [t f]))

(extend-protocol Functor
   
   java.lang.Object
   (fmap [v f] (f v))
   
   nil
   (fmap [v f] nil)
   
   clojure.lang.Seqable
   (fmap [v f] (map f v))
   
   SharedIO
   (fmap [ {:keys [k io]} f ]
        ((:send shared-io-star-chan) k f io))
  )


                 