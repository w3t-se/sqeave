(ns main
  (:require ["@w3t-ab/sqeave" :as sqeave]
            ["@w3t-ab/sqeave/components/jsonviewer" :as jsonv]
            ["solid-js/web" :refer [Dynamic]]
            ["solid-js" :as s :refer [Switch Match]])
  (:require-macros [sqeave :refer [defc]]))

(defc Main1 [this {:main/keys [id count] :or {id 1 count 0}}]
  #jsx [:div {} "Hello Sqeave Main " (id)
        [:button {:onClick #(sqeave/set! this :main/count (inc (count)))} "Plus"]
        [:p {} "Main 1 Count: " (count)]])

(defc Main2 [this {:main/keys [id count] :or {id 2 count 0}}]
  #jsx [:div {} "Hello Sqeave Main " (id)
        [:button {:onClick #(sqeave/set! this :main/count (inc (count)))} "Plus"]
        [:p {} "Main 2 Count: " (count)]])

(defc Main3 [this {:main/keys [id count] :or {id (sqeave/uuid) count 0}}]
  (let [[type setType] (s/createSignal :main1)
        cid2c {:main1 {:comp Main1 :ident [:main/id 1]}
               :main2 {:comp Main2 :ident [:main/id 2]}}]
    #jsx [:div {} "Hello Sqeave Main " (id)
          [:button {:onClick #(setType :main1)} "Show Main1"]
          [:button {:onClick #(setType :main2)} "Show Main2"]
          [:button {:onClick #(sqeave/set! this :main/count (inc (count)))} "Plus"]
          [:p {} "Main 3 Count: " (count)]

          [Dynamic {:& {:component (get-in cid2c [(type) :comp])
                        :ident (get-in cid2c [(type) :ident])}}]]))

(defc Main [this {:main/keys [id count] :or {id (sqeave/uuid) count 0}}]
  #jsx [:div {}
        [:h1 "Hello Sqeave Main " (id)]
        [Main3 {:ident [:main/id "abc"]}]
        [:h2 {} "Store"]])
