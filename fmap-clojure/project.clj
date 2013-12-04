(defproject fmap-clojure "0.1.0"
  :description "Clojure monad library by using functors and copying to simplicity of haskell"
  :url "https://github.com/gerritjvv/fmap-clojure/tree/master/fmap-clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
 
  :plugins [
         [lein-midje "3.0.1"] [lein-marginalia "0.7.1"]
         [lein-kibit "0.0.8"] [no-man-is-an-island/lein-eclipse "2.0.0"]
           ]
  :warn-on-reflection true

  :dependencies [
                 [fun-utils "0.1.0"]
                 [midje "1.6-alpha2" :scope "test"]
                 [org.clojure/clojure "1.5.1"]])
