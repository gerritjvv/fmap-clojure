(ns fmap-clojure.applicative-tests
  (:require [fmap-clojure.core :refer [<*> >>=*]])
  (:use midje.sweet))

(facts "Test <*>"
       
       (fact "Test <*> over single argument"
             (<*> [inc] [1]) => [2])
       (fact "Test <*> over 4 arguments"
             (<*> [inc] [1 2 3 4]) => [2 3 4 5])
       (fact "Test <*> over 1 int and a nested list of 4 arguments"
             (<*> [inc] [1 [1 2 3 4]]) => [2 [2 3 4 5]])
       (fact "Test fmap for maps"
             (prn 
               (>>=* [{:topic "a" :partition 1 :val "hi"} 
                      {:topic "b" :partition 2 :val "hi2"}]
                      (partial group-by :topic))
               ))
                   
       )
