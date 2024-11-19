(ns index
  (:require ["solid-js/web" :refer [render]]
            ["solid-js/store" :refer [createStore]]
            ["@w3t-ab/sqeave/comp" :as comp]
            ["./Context.cljs" :refer [AppContext]]
            ["./main.cljs" :refer [UiMain]])
  (:require-macros [comp :refer [defc def-factory]]))

(defc Root [this {:keys []}]
  (let [[store setStore] (createStore {:click/id {0 {:click/id 0
                                                     :click/count 0}}})]
    #jsx [AppContext.Provider {:value {:store store :setStore setStore}}
          [UiMain {:& {:ident (fn [] [:click/id 0])}}]]))

(def-factory UiRoot Root AppContext RootFn)

(render UiRoot (js/document.getElementById "root"))
