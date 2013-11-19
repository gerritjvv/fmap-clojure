(ns fmap-clojure.core
  (:require [fun-utils.core :refer [star-channel]])
  )


(defrecord SharedIO [ k io ])

(defn IO [ io & {:keys [k] :or {k (hash io)}}]
  "Creates a SharedIO instance from io and if k is not defined uses the (hash io) as the key"
  (SharedIO. k io))

(def shared-io-star-chan (star-channel))

(defprotocol Functor
     (fmap [t f])
     (lift [t]))

;just is used when a data type implements 
;several interfaces and fmap cannot be clearly applied
;but for most of the times use the value and not just
(defrecord Just [v])

(extend-protocol Functor
   
   java.lang.Object
   (fmap [v f] (f v))
   (lift [v] v)
   
   Just
   (fmap [v f] (Just. (f (:v v))))
   (lift [v] (:v v))
   
   nil
   (fmap [v f] nil)
   (lift [v] nil)
   
   clojure.lang.IFn
   (fmap [v f] (prn "comp : " (type v))(comp f v))
   (lift [v] v)
   
   clojure.lang.Seqable
   (fmap [v f] (map f v))
   (lift [v] v)
   
   clojure.lang.PersistentVector
   (fmap [v f] (map f v))
   (lift [v] v)
   
   java.util.Collection
   (fmap [v f] (map f v))
   (lift [v] v)
   
   SharedIO
   (fmap [ {:keys [k io]} f ]
        ((:send shared-io-star-chan) k f io))
   (lift [v] v)
   
  )

(defn <*> [fs args]
  "Applicative function that applies each function in f over each argument in a,
   the application of each function is done using fmap so that if argument a is a list
   the function is again applied as map f a"
  (for [f fs 
        a args] (fmap a f)))

(defn ID [x] x)

(defmacro f [& args]
  "Functional composition macro, that allows us to compose functions implicitely using fmap
   and explictely using <*> to move data through logic all composed from left to right.
   (f 1 f1 [f2 f3] <*> [b c] f4) => (apply f4 (fmap f1 a) (for [p [f2 f3] q [b c]] (fmap p a)) )
   or
   (1 2 3 4 inc) => (apply inc 1 2 3 4)
   "
  )

;;(fmap* 1 2 +)
;;(fmap* + 1 2)
;;(fmap* io-read)
;;(fmap* io-read readLine upper)
;;(fmap* io-read readLine upper io-print)
;;fmap 1 => return 1
;;fmap fun =>
                 