(ns main
  (:require ["@w3t-ab/sqeave" :as sqeave]
            ["solid-js/web" :refer [Dynamic]]
            ["solid-js" :as s :refer [Switch Match]])
  (:require-macros [sqeave :refer [defc]]))

#_(when (.-hot js/import.meta)
  (.accept (.-hot js/import.meta) (fn []
                                    (set! js/window.hot true))))

(defc Main2 [this {:main/keys [id count] :or {id (sqeave/uuid) count 0}}]
  #_(s/onMount (fn [] (println "asda")))
  #jsx [:div {} "Hello Sqeave Main2: " (id)
        [:button {:onClick #(do (println "asd:" this) (sqeave/set! this :main/count (inc (count))))} "Plus"]
        [:p {} "Count: " (count)]])

(defc Main4 [this {:main/keys [id count] :or {id (sqeave/uuid) count 0}}]
  #_(s/onMount (fn [] (println "asda")))
  #jsx [:div {} "Hello Sqeave Mai4: "
        [:button {:onClick #(sqeave/set! this :main/count (inc (count)))} "Plus"]
        [:p {} "Count: " (count)]])

(defc Main3 [this {:dido/keys [id count] :or {id (sqeave/uuid) count 0}}]
  (let [[type setType] (s/createSignal :local-store)
        cid2c {:local-store (fn [props] #jsx [Main2 {:& {:ident [:main/id 123]}}])
               :openapi (fn [props]
                          #jsx [Main4 {:& {:ident [:main/id 321]}}])}]
    #jsx [:div {} "Hello Sqeave Main3: " (second this.ident)
          [:button {:onClick #(do
                                (setType :openapi)
                                (sqeave/set! this :dido/count (inc (count))))} "Plus"]
          [:button {:onClick #(do
                                (setType :local-store))} "local-store"]
          [:p {} "Count: " (count)]
          #_[Dynamic {:& {:component (get cid2c (type))
                          :ident this.ident}}]

          [Switch {:fallback (fn [] #jsx [:div {} "NFaa"])}
           [Match {:when (= (type) :local-store)}
            #jsx [Main2 {:& {:ident [:main/id 123]}}]]
           [Match {:when (= (type) :local-store)}
            #jsx [Main4 {:& {:ident [:main/id 321]}}]]]]))

(defc Main [this {:main/keys [id count] :or {id (sqeave/uuid) count 0}}]
  (let [[mains setMains] (s/createSignal [])
        onClick (fn [e] (do
                          #_(println "st: " this)
                          (setMains (conj (mains) [:main/id 123]))
                          #_(this.ctx.setStore (assoc-in this.ctx.store [:main/id 0 :main/count] (sqeave/uuid)))
                          #_(sqeave/set! this :main/count (inc (count)))))]

    #jsx [:div {} "Hello Sqeave Mainaa: " this.ident
          [:button {:onClick onClick} "Plus"]
          [:p {} "Count: " (count)]
          [s/For {:each (mains)}
           (fn [node _]
             #jsx [Main2 {:ident node}])]
          [Main3 {:ident [:dido/id :main]}]
          [Main3 {:ident [:dido/id 2]}]
          [Main3 {:ident [:dido/id 2]}]]))
