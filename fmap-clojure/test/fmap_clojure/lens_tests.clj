(ns fmap-clojure.lens-tests
  (:require
    [fmap-clojure.core :refer :all]
    [fmap-clojure.lens :refer :all]
    [midje.sweet :as midje]))


(midje/facts "Test lenses"
       (midje/fact "Simple View"

             (-> 3 (view id)) midje/=> 3
             (-> 3 (update id inc)) midje/=> 4
             (-> 3 (put id 7)) => 7)
       (midje/fact "Test in"

             (-> {:value 3} (view (in [:value]))) midje/=> 3
             (-> {:value 3} (update (in [:value]) inc)) midje/=> {:value 4}
             (-> {:value 3} (put (in [:value]) 7)) midje/=> {:value 7})
       (midje/fact "Test combine =>"

             (-> {:new {:value 3}} (view (=> (in [:new]) (in [:value])))) midje/=> 3
             (-> {:new {:value 3}} (update (=> (in [:new]) (in [:value])) inc)) midje/=> {:value 4}
             (-> {:new {:value 3}} (put (=> (in [:new]) (in [:value])) 7)) midje/=> {:value 7})
       (midje/fact "Test each"

             (-> {:values [3 4 5]} (view (=> (in [:values]) each))) midje/=> [3 4 5]
             (-> {:values [3 4 5]} (update (=> (in [:values]) each) inc)) midje/=> {:values [4 5 6]}
             (-> {:values [3 4 5]} (put (=> (in [:values]) each) 7)) midje/=> {:values [7 7 7]}
             (-> {:values [(atom 3) (atom 4) (atom 5)]} (put (=> (in [:values]) each) 7) (update (=> (in [:values])) lift)) midje/=> {:values [7 7 7]})
       (midje/fact "Test update values"

             (-> {:x 3 :y 4 :z 5} (view values)) => '(5 4 3)
             (-> {:x 3 :y 4 :z 5} (update values inc) => {:x 4, :y 5, :z 6})
             (-> {:x 3 :y 4 :z 5} (put values 7)) => {:x 7, :y 7, :z 7})

       (midje/fact "Test update keys"

             (-> {3 :x 4 :y 5 :z} (view all-keys)) midje/=> '(3 4 5)
             (-> {3 :x 4 :y 5 :z} (update all-keys inc)) midje/=> {6 :z, 5 :y, 4 :x}
             (-> {3 :x 4 :y 5 :z} (put all-keys 7)) midje/=> {7 :z} ;this is because the key is 7
             )
       (midje/fact "Test filters"


             (-> [3 4 5] (view (only odd?))) midje/=> '(3 5)
             (-> [3 4 5] (update (only odd?) inc)) midje/=> [4 4 6]
             (-> [3 4 5] (put (only odd?) 7)) midje/=> [7 4 7]))
