(ns fmap-clojure.monad
  (:require [fmap-clojure.core :refer [Functor fmap]]))

"Implements the state Monad using a type record. 
   x is the value and s the state. 
   A function applied to the type State must take two arguments, the first is x and the second s (value and state),
   and normally returns an instance of State
   e.g.
   (defn statetransformer [x s]
       (->State (inc x) (conj s :inc)))
  
   Functions applied to the State are called state transformers.
   For reference see: http://learnyouahaskell.com/for-a-few-monads-more

   A suble change from normal monads is that unit and join are not implemented as methods per type.
   unit is equal to instantiating a type, and join can be implemented using mapcat concat etc.
 "
(defrecord State [x s]
 
  Functor
  (fmap [t f]
    (f (:s t)))
  
  (lift [t]
    (:x t)))

(defn state [x s] (->State x s))

(comment ;EXAMPLE State Stack
  "
   The haskell implementation is
		type Stack = [Int]  
		  
		pop :: Stack -> (Int,Stack)  
		pop (x:xs) = (x,xs)  
		  
		push :: Int -> Stack -> ((),Stack)  
		push a xs = ((),a:xs)  

    Below is the clojure fmap implementation
   "         
   (defn pop* []
     (fn [s]
       (state (peek s) (pop s))))
    
   (defn push [v]
     (fn [s]
       (state nil (conj s v))))
   
   (=
     (>>=* (state nil [1 2]) (push 3) (push 4) (push 5) (pop*) (pop*) lift)
     [1 2 3])
   
   )




