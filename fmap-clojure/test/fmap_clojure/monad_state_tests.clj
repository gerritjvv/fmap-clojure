(ns fmap-clojure.monad-state-tests
  (:require [fmap-clojure.core :refer [>>=* lift]]
            [fmap-clojure.monad :refer [state]]
            [fmap-clojure.monad-impl :refer [push pop*]])
  (:use midje.sweet))

(facts "Test state monads"
       (fact "Test using Stack"
             (>>=* (state nil [1 2]) (push 3) (push 4) (push 5) (pop*) (pop*) lift) => [1 2 3]
             ))