# fmap-clojure

 Under construction 

# Rationale

I've always been searching for a more concise and expressive way to write programs, 
and when I read [Can programming be liberated from the von Neumann style](http://dl.acm.org/citation.cfm?id=359579) I got sold on functional programming.

Functional programming provides many highlevel constructs to save us from retyping common patterns like looping e.g. map or reduce, 
the simple map and reduce help us to reason better about the logic in our programs.
Now we can concentrate on the function that transforms, and the function that aggregates, then apply these to a collection using map and or reduce, rather than having
to write boiler plate looping and combining code over and over again.

When working with functions you realise that many functions can be more easily composed, using functional composition, this is yet one level up from map.
Using functional composition programs become much clearer, concise and expressive, and I'm not talking about ```(comp f g)``` but rather about composing
functions and data with simple constructs so that the logic and meaning is both clear and concise.

Two such constructs that are central to functional composition is: fmap and <*> or in other words Functors and Applicatives.

## The path to elegant functional composition

To be able to do functional composition elegantly we need some high level constructs, just as functional programming builds on map, reduce, filter etc, 
we will build on fmap, and <*> or otherwise said Functors and Applicatives.

### fmap

Functors in essence are nothing more than a type implementation that defines how a function should be applied to a certain type.
So that if we say ```(fmap 1 inc)``` the functor implementation for Object would apply 1 to inc, if we said ```(fmap nil inc)```
the functor implementation for nil would return nil directly and not apply the function.

Now there is some dellusion arround the Maybe Functor Monad etc... its application is simple and not magical,
it goes as if a value apply it to the function if no value do not apply (thats it). In clojure we do this by
providing functor implementations for nil and Object.

Functors in clojure can be implemented using protocols and extend type, i.e. we create a protocol Functor with a function fmap.

```clojure 
(defprotocol Functor
     (fmap [t f]))
```

and then provide some basic typed implementations

```clojure
(extend-protocol Functor
   
   java.lang.Object
   (fmap [v f] (f v))
   (lift [v] v)
   
   Just
   (fmap [v f] (Just. (f (:v v))))
   (lift [v] (:v v))
   
   nil
   (fmap [v f] nil)
   (lift [v] nil)
   
   clojure.lang.IFn
   (fmap [v f] (prn "comp : " (type v))(comp f v))
   (lift [v] v)
   
   clojure.lang.Seqable
   (fmap [v f] (map f v))
   (lift [v] v)
   
   clojure.lang.PersistentVector
   (fmap [v f] (map f v))
   (lift [v] v)
   
   java.util.Collection
   (fmap [v f] (map f v))
   (lift [v] v)
   
   )
```

Now we can write:

```
(fmap nil inc)
;; nil
(fmap 1 inc)
;; 2

```

Using fmap on a function composes the two functions using comp, using fmap over a sequence is the same as using map.

Now you might ask why not use map and apply the functions directly to their arguments? This is a valid question and for most part
you would be right, but using fmap gives as a single uniform way of applying a function f to a argument a without needing to know
if its a sequence, object, nil a function etc, having this abstraction helps us build more concise code in where with a single call ```(fmap arg f)```
the correct logic is applied depending on the object type, it even handles nil correctly, thus giving us Maybe.

A word on Just:

In Haskell Just is used always to wrap a value before applying to a functor, but here we use it as a last resort when you want to pass in a sequence
to a function not using map but actually as a single argument. If you introduce Just into an expression the result will always be Just, to lift the value
out of the Just we use another function called lift, which when called on Just returns the value inside Just, when called on anything else must return
the same value as passed into the function, allowing us to always put lift at the end of a chain of expressions.

Example

```
(fmap [1 2 3] inc)
;; [2 3 4]
(fmap (->Just [1 2 3]) count)
;; (Just 3)
(-> (fmap (->Just [1 2 3]) count) (fmap str) lift)
```

### <*> the applicative

Now Applicative is just a fancy name for a function that takes a sequence of functions and apply them to a sequence returning the combined results.

In Haskell this is ```<*>``` and in clojure we can use the same naming so that we can do:

```clojure
(defn <*> [fs args]
  (for [f fs 
        a args] (fmap a f)))
        
(<*> [inc dec] [1 2])
;; (2 3 0 1)

(<*> [inc] [1 [2 3]])
;; (2 (3 4))
```


## Overview Message passing




## License

Distributed under the Eclipse Public License either version 1.0
