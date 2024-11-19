(ns main
  (:require ["@w3t-ab/sqeave/comp" :as comp]
            ["./Context.cljs" :refer [AppContext]])
  (:require-macros [comp :refer [defc def-factory]]))

(defc Main [this {:click/keys [id count] :or {id (comp/uuid) count 0}}]
  #jsx [:div {} "Hello Sqeave: "
        [:button {:onClick #(comp/set! this :click/count (inc (count)))} "Plus"]
        [:p {} "Count: " (count)]])

(def-factory UiMain Main AppContext MainFn)
