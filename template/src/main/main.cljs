(ns main
  (:require ["@w3t-ab/sqeave" :as sqeave])
  (:require-macros [sqeave :refer [defc]]))

(defc Main [this {:click/keys [id count] :or {id (sqeave/uuid) count 0}}]
  #jsx [:div {} "Hello Sqeave: "
        [:button {:onClick #(sqeave/set! this :click/count (inc (count)))} "Plus"]
        [:p {} "Count: " (count)]])
