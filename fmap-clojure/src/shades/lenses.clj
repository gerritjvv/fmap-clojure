(ns shades.lenses)
;copied from https://gist.github.com/ctford/15df66f029f70b986121
;the idea for this library is to extend this lense implementation to use fmap and <*> 
; We only need three fns that know the structure of a lens.
(defn lens [focus fmap] {:focus focus :fmap fmap})
(defn view [x {:keys [focus]}] (focus x))
(defn update [x {:keys [fmap]} f] (fmap f x))
 
; The identity lens.
(defn fapply [f x] (f x))
(def id (lens identity fapply))
 
; Setting can be easily defined in terms of update.
(defn put [x l value] (update x l (constantly value)))
 
(-> 3 (view id))
(-> 3 (update id inc))
(-> 3 (put id 7))
 
; in makes it easy to define lenses based on paths.
(defn in [path]
  (lens
    (fn [x] (get-in x path))
    (fn [f x] (update-in x path f))))
 
(-> {:value 3} (view (in [:value])))
(-> {:value 3} (update (in [:value]) inc))
(-> {:value 3} (put (in [:value]) 7))
 
; We can combine lenses.
(defn combine [outer inner]
  (lens
    (fn [x] (-> x (view outer) (view inner)))
    (fn [f x] (update x outer #(update % inner f)))))
 
(defn => [& lenses] (reduce combine lenses))
 
(-> {:new {:value 3}} (view (=> (in [:new]) (in [:value]))))
(-> {:new {:value 3}} (update (=> (in [:new]) (in [:value])) inc))
(-> {:new {:value 3}} (put (=> (in [:new]) (in [:value])) 7))
 
; We also allow for multiple foci.
(def each (lens seq map))
 
(-> {:values [3 4 5]} (view (=> (in [:values]) each)))
(-> {:values [3 4 5]} (update (=> (in [:values]) each) inc))
(-> {:values [3 4 5]} (put (=> (in [:values]) each) 7))
 
(-> {:new {:values [3 4 5]}} (view (=> (in [:new]) (in [:values]) each)))
(-> {:new {:values [3 4 5]}} (update (=> (in [:new]) (in [:values]) each) inc))
(-> {:new {:values [3 4 5]}} (put (=> (in [:new]) (in [:values]) each) 7))
 
; We can do things like focus on all the keys or values in a map.
(def values
  (lens
    vals
    (fn [f x] (->> x vals (map f) (zipmap (keys x))))))
 
(-> {:x 3 :y 4 :z 5} (view values))
(-> {:x 3 :y 4 :z 5} (update values inc))
(-> {:x 3 :y 4 :z 5} (put values 7))
 
(def all-keys 
  (lens
    keys 
    (fn [f x] (zipmap (map f (keys x)) (vals x)))))
 
(-> {3 :x 4 :y 5 :z} (view all-keys))
(-> {3 :x 4 :y 5 :z} (update all-keys inc))
(-> {3 :x 4 :y 5 :z} (put all-keys 7))
 
; We can focus based on a predicate.
(defn fwhen [pred?] (fn [f x] (if (pred? x) (f x) x)))
(defn only [pred?]
  (lens
    (fn [x] (filter pred? x))
    (fn [f x] (map (partial (fwhen pred?) f) x))))
 
(-> [3 4 5] (view (only odd?)))
(-> [3 4 5] (update (only odd?) inc))
(-> [3 4 5] (put (only odd?) 7))
 
; We can focus on certain parts of a sequence.
(def evens
  (let [index (partial map vector (range))
        deindex (partial map second)
        applicable? (comp even? first)]
    (lens
      (fn [x] (-> x index (view (only applicable?)) deindex))
      (fn [f x] (-> x index (update (only applicable?) (fn [[i v]] [i (f v)])) deindex)))))
 
(-> [3 4 5] (view evens))
(-> [3 4 5] (update evens inc))
(-> [3 4 5] (put evens 7))
 
; Nil safety
(def fmaybe (fwhen (complement nil?)))
(def maybe (lens identity fmaybe))
 
(-> nil (view maybe))
(-> 3 (view maybe))
(-> [3 nil 5] (view (=> each maybe)))
(-> [3 nil 5] (update (=> each maybe) inc))
(-> [3 nil 5] (put (=> each maybe) 7))

