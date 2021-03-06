(ns fmap-clojure.core
  (:import
    (java.util Map Iterator Collection)
    (clojure.lang IPersistentVector Seqable IPersistentMap IFn Atom IRef PersistentVector))
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

   IFn
   (fmap [v f] (comp f v))
   (lift [v] v)

   Map
   (fmap [v f] (f v))
   (lift [v] v)

   IPersistentMap
   (fmap [v f] (f v))
   (lift [v] v)

   
   Seqable
   (fmap [v f] (map #(fmap % f) v))
   (lift [v] v)
   
   Iterable
   (fmap [^Iterable v f]
     (fmap (iterator-seq (.iterator v)) f))
   (lift [v] v)
   
   Iterator
   (fmap [^Iterator v f]
     (fmap (iterator-seq v) f))
   (lift [v] v)

   PersistentVector
   (fmap [v f] (mapv #(fmap % f) v))
   (lift [v] v)

   Collection
   (fmap [v f] (map #(fmap % f) v))
   (lift [v] v)

   SharedIO
   (fmap [ {:keys [k io]} f ]
        ((:send shared-io-star-chan) k f io))
   (lift [v] v)

   ;;clojure reference types
   Atom
   (fmap [v f]
     (atom (swap! v f)))
   (lift [v] (deref v))

   IRef
   (fmap [v f]
     (dosync
       (ref (alter v f))))
   (lift [v]
     (deref v))
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
   
   clojure.lang.IPersistentVector
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
                     (= (second body) :jl)
                        [(list `>>=* (first body) `:just (nth body 2) `:lift) (drop 3 body)]
                     (= (second body) :just)
                        [(list `just (first body)) (drop 2 body)]   
                     (= (second body) :>>=)
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
                 