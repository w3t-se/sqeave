(ns main
  (:require ["@w3t-ab/sqeave" :as sqeave])
  (:require-macros [sqeave :refer [defc]]))

(defc Main [this {:main/keys [id count] :or {id 0 count 0}}]
  #jsx [:div {} "Hello Sqeave: "
        [:button {:onClick #(sqeave/set! this :main/count (inc (count)))} "Plus"]
        [:p {} "Count: " (count)]])
