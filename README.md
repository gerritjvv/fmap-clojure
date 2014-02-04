# fmap-clojure

Clojure monad library by using functors and copying to simplicity of haskell

# Setup

Leiningen

```[fmap-clojure "0.1.5"]```

gradle 

```compile "fmap-clojure:fmap-clojure:0.1.5"```

Maven

```
<dependency>
  <groupId>fmap-clojure</groupId>
  <artifactId>fmap-clojure</artifactId>
  <version>0.1.5</version>
</dependency>
```

# Namespace

ns

``` (:require [fmap-clojure.core :refer [>>=* >>= fmap just lift]]) ```

Repl

```(use 'fmap-clojure.core)```


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

# The path to elegant functional composition

To be able to do functional composition elegantly we need some high level constructs, just as functional programming builds on map, reduce, filter etc, 
we will build on fmap, and <*> or otherwise said Functors and Applicatives.

## fmap >>=

Functors in essence are nothing more than a type implementation that defines how a function should be applied to a certain type.
So that if we say ```(fmap 1 inc)``` the functor implementation for Object would apply 1 to inc, if we said ```(fmap nil inc)```
the functor implementation for nil would return nil directly and not apply the function.

Now there is some delusion arround the Maybe Functor Monad etc... its application is simple and not magical,
it goes as if a value apply it to the function if no value do not apply (thats it). In clojure we do this by
providing functor implementations for nil and Object.

Throughout the code ```>>=``` and ```fmap``` means the same, there is a separate ```(def >>= fmap)``` statement in the library.

Functors in clojure can be implemented using protocols and extend type, i.e. we create a protocol ```Functor``` with a function ```fmap``` and ```lift```,
lift takes a value in some context and lifts it out of the context returning the lifted value, e.g. ```(lift (->Just 3))``` returns 3.


```clojure 
(defprotocol Functor
     (fmap [t f])
     (lift [v]))
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

```clojure
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

```clojure
(fmap [1 2 3] inc)
;; [2 3 4]
(>>= [1 2 3] inc)
;; [2 3 4]
(fmap (->Just [1 2 3]) count)
;; (Just 3)
(-> (fmap (->Just [1 2 3]) count) (fmap str) lift)
```

## <*> the applicative

Now Applicative is just a fancy name for a function that takes a sequence of functions and apply them to a sequence returning the combined results,
another definition would be taking a function with a context and applying it to a value inside a context.

In Haskell this is <*> and in clojure we can use the same naming so that we can do:

```clojure
(defn <*> [fs args]
  (for [f fs 
        a args] (fmap a f)))
        
(<*> [inc dec] [1 2])
;; (2 3 0 1)

(<*> [inc] [1 [2 3]])
;; (2 (3 4))
```


## What do we have now?

fmap and <*>,  are two functions that apply values to functions and boxed functions to values in a generic way using only two functions.
For most of the time we can only use fmap (>>=) , and <*> uses fmap internally.

In other words, we have functors and on top of functors we have applicatives, but most of the time we will only need functors.

Functional composition is much more that just composing two functions, its about how to apply functions two data and how data
flows between a series of functions. It allows us to write our functions separately and then write out final logic out as a
series of data and functions binding each step to the next. 


## Left right data flow

The macro ```>>=*``` applies fmap (>>=) to the expressions in such a way that if we write ```(>>=* [1 2 3]  inc dec)```
the expression is translated to ```(>>= (>>= [1 2 3] inc) dec)```. Writing expressions from left to right instead of nested makes the code
easier to read and comprehend.

The ```>>=*``` macro has a few directives to change the way data and functions are applied.

## :just, :lift and :jl ##

For ```:just``` the result of the expression so far is translated into a ```Just``` instance, the ```:lift``` directive
will apply the result to the lift function and for ```Just``` this means extracting the value from the ```Just``` context.

The keyword ```:jl``` is a shorthand for writing ```expr :just expr :lift```, with
```:jl``` this becomes ```expr :jl expr```

This is useful if you want to pass a list as a function argument.

e.g.

```clojure
(>>=* [1 2 3] count)
;; fails because count is applied to 1 then 2 and then 3
(>>=* [1 2 3] inc vector count)
;; (1 1 1)
(>>=* [1 2 3] inc :just count)
;; fmap_clojure.core.Just{:v 3}
(>>=* [1 2 3] inc vector :just count :lift)
;; 3
(>>=* [1 2 3] inc vector :jl count )
;; 3

```


## :apply ##

Apply uses three arguments, one; the result of the expression so far on the left hand side, two; ```:apply``` directory, three: the function on the immediate right of ```:apply```.
The expression is rewritten as (apply f result).

e.g.

```clojure 

(>>=* [1 2 3] inc dec str)
;; ["1" "2" "3"]
(>>=* [1 2 3] inc dec :apply str)
;; "123"

```

## Examples ##

```clojure

 (>>=* [1 2 3] inc dec :just) 
   ;; fmap_clojure.core.Just{:v 3}
 (>>=* [1 2 3] inc dec :just count :lift)
   ;; 3
 (>>=* [1 2 3] inc dec :jl count)
   ;; 3
 (>>=* [1 2 3] inc dec :apply str)
   ;; "123"
 (>>=* [1 2 3] inc dec str)
   ;; ["1" "2" "3"]
   
```

## Conclusion >>=* ##

```>>>=*``` gives us ```fmap``` (```>>=*```), ```:lift```, ```:just```, ```:apply``` as tools to flow data through a series of expressions. 

The logic is extendible to specific types by extending the Functor protocol.

   
   
## Monads ##

The state monad.

## License

Distributed under the Eclipse Public License either version 1.0
