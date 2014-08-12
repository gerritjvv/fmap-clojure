(ns fmap-clojure.dataflow-tests
  (:import (clojure.lang Atom))
  (:require [fmap-clojure.core :refer [>>=*]])
  (:use midje.sweet))

(facts "Test threading"
       (fact "Test fmap"
             (>>=* [1 2 3] inc :just (fn [x] (reduce + x)) :lift) => 9)

       (fact "Test just lift"
             (>>=* [1 2 3] inc vector :just count :lift) => 3)

       (fact "Test apply"
             (>>=* [1 2 3] inc dec :apply str) => "123"))
