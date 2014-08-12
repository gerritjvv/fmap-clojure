(ns fmap-clojure.reference-type-tests
  (:import (clojure.lang Atom))
  (:require [fmap-clojure.core :refer [>>=*]])
  (:use midje.sweet))

(facts "Test types"
       (fact "Test Atom"
             (>>=* (atom 1) inc inc :lift) => 3)
       (fact "Test Ref"
             (>>=* (ref 1) inc inc :lift) => 3))

