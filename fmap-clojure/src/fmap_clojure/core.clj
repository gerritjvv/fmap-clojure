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

(defprotocol Applicative
    (<*> [v f]))

;just is used when a data type implements 
;several interfaces and fmap cannot be clearly applied
;but for most of the times use the value and not just
(defrecord Just [v])

(defn just [v]
  (->Just v))

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

(extend-protocol Applicative
   
   java.lang.Object
   (<*> [fs args]
     (fmap fs args))
   
   Just
   (<*> [fs args]
      (fmap args (:v fs)))
   
   nil
   (<*> [f args] nil)
   
   clojure.lang.IFn
   (<*> [f args]
      (fmap args f))
   
   clojure.lang.Seqable
   (<*> [fs args]
      (for [f fs 
        a args] (fmap a f)))
   
   clojure.lang.PersistentVector
   (<*> [fs args]
      (for [f fs 
        a args] (fmap a f)))
   
   java.util.Collection
   (<*> [fs args]
      (for [f fs 
        a args] (fmap a f)))
   
   SharedIO
   (<*> [fs args]
     (fmap args fs))
   
  )

(defn ID [x] x)

(defmacro f [& args]
  "Functional composition macro, that allows us to compose functions implicitely using fmap
   and explictely using <*> to move data through logic all composed from left to right.
   (f 1 f1 [f2 f3] <*> [b c] f4) => (apply f4 (fmap f1 a) (for [p [f2 f3] q [b c]] (fmap p a)) )
   or
   (1 2 3 4 inc) => (apply inc 1 2 3 4)
   "
  )

(def >>= fmap)

(defmacro >>=* [& body]
  "
   (>>=* [1 2 3] inc dec :just) 
   ;; fmap_clojure.core.Just{:v 3}
   (>>=* [1 2 3] inc dec :just count :lift)
   ;; 3
   (>>=* [1 2 3] inc dec :apply str)
   ;; \"123\"
   (>>=* [1 2 3] inc dec str)
   ;; [\"1\" \"2\" \"3\"]
  "
       (let [cnt (count body)]
           (cond (> cnt 1)
			       (let [[e1 e2]
			             (cond 
                     (= (second body) :just)
                        [(list `just (first body)) (drop 2 body)]
                     (= (second body) :lift)
                        [(list `lift (first body)) (drop 2 body)]
                     (= (second body) :apply)
                     (if (> cnt 2)
                         [ (list `apply (nth body 2) (list `lift (first body) ) ) (drop 3 body)]
                         1
                       )
			               :else
			               [(list `>>= (first body) (second body)) (drop 2 body)])]
			             
			             (cons `>>=* (cons e1 e2)))
              (= cnt 1) 
               (first body)
              :else
               body
            )))
			             
             
       
  
  

;;(fmap* 1 2 +)
;;(fmap* + 1 2)
;;(fmap* io-read)
;;(fmap* io-read readLine upper)
;;(fmap* io-read readLine upper io-print)
;;fmap 1 => return 1
;;fmap fun =>
                 