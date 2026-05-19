(ns index
  (:require ["solid-js"]
            ["solid-js/web" :refer [render]]
            ["@w3t-ab/sqeave" :as sqeave]
            ["@w3t-ab/sqeave/devtools" :as devtools]
            ["./main.cljs" :refer [Main]])
  (:require-macros [sqeave :refer [defc]]))

(defc Root [this {:keys [] :or {}}]
  #jsx [:div {}
        [Main {:ident [:main/id 0]}]
        [devtools/DevTools {:ctx this.ctx}]])

(let [e (js/document.getElementById "root")]
  (set! (aget e :innerHTML) "")
  (render (fn []
            #jsx [sqeave/SqeaveProvider {}
                  [Root {}]]) e))
