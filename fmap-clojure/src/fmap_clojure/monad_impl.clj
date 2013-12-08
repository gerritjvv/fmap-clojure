(ns fmap-clojure.monad-impl
  (:require [fmap-clojure.core :refer [>>=* lift]]
            [fmap-clojure.monad :refer [state]]))

"Implementations showing the usage of the monads created in monad"

;;Implementing a stack
;; http://learnyouahaskell.com/for-a-few-monads-more

(defn pop* []
     (fn [s]
       (state (peek s) (pop s))))
    
(defn push [v]
     (fn [s]
       (state nil (conj s v))))
   
(= (>>=* (state nil [1 2]) (push 3) (push 4) (push 5) (pop*) (pop*) lift)
   [1 2 3])