(defproject fmap-clojure "0.1.5"
  :description "Clojure monad library by using functors and copying to simplicity of haskell"
  :url "https://github.com/gerritjvv/fmap-clojure/tree/master/fmap-clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]

  :plugins [
         [lein-midje "3.0.1"] [lein-marginalia "0.7.1"]
         [lein-kibit "0.0.8"] [no-man-is-an-island/lein-eclipse "2.0.0"]
           ]
  :warn-on-reflection true

  :dependencies [
                 [fun-utils "LATEST"]
                 [midje "1.6-alpha2" :scope "test"]
                 [org.clojure/clojure "1.5.1"]])
