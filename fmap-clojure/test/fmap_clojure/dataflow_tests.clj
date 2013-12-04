(ns fmap-clojure.dataflow-tests
  (:require [fmap-clojure.core :refer [>>=*]])
  (:use midje.sweet))

(facts "Test threading"
       (fact "Test fmap"
             (>>=* [1 2 3] inc vector count) => '(1 1 1))
             
       (fact "Test just lift"
             (>>=* [1 2 3] inc vector :just count :lift) => 3)
       
       (fact "Test apply"
             (>>=* [1 2 3] inc dec :apply str) => "123"))

