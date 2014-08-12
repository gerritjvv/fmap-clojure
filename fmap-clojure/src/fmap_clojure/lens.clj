(ns fmap-clojure.lens
  (:import (clojure.lang PersistentVector)
           (java.util Collection))

  (:require [fmap-clojure.core :as core]))

;;Simple rule for using fmap
;; core/fmap goes inside the value i.e for collections we are inside the collection itself.
(defprotocol LenseSeqable
  (to-seq [x]))

(extend-protocol LenseSeqable
    PersistentVector
    (to-seq [x]
      x)
    Collection
    (to-seq [x] x)
    Object
    (to-seq [x] (seq x)))

(defrecord Lens [focus fmap])

(defn lens [focus fmap] (->Lens focus fmap))
(defn view [x {:keys [focus]}] (focus x))                   ;focus should not use fmap, it needs to point at the exact value
(defn update [x {:keys [fmap]} f] (core/fmap x (partial fmap f)))

; The identity lens.
(defn fapply [f x] (core/fmap x f))
(def id (lens identity fapply))

; Setting can be easily defined in terms of update.
(defn put [x l value] (update x l (constantly value)))

(defn in [path]
  (lens
    (fn [x] (get-in x path))
    (fn [f x] (update-in x path (partial fapply f)))))

(defn combine [outer inner]
  (lens
    (fn [x] (-> x (view outer) (view inner)))
    (fn [f x] (update x outer #(update % inner f)))))

(defn => [& lenses] (reduce combine lenses))

(def each (lens to-seq fapply))

(def values
  (lens
    vals
    (fn [f x] (->> x vals (map f) (zipmap (keys x))))))

(def all-keys
  (lens
    keys
    (fn [f x] (zipmap (map f (keys x)) (vals x)))))

(defn fwhen [pred?] (fn [f x] (if (pred? x) (f x) x)))
(defn only [pred?]
  (lens
    (fn [x] (filter pred? x))
    (fn [f x]
      ;(map (partial (fwhen pred?) f) x) we're using fmap so we are already inside the collection
      ((partial (fwhen pred?) f) x))))

