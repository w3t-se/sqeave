(ns index
  (:require ["solid-js/web" :refer [render]]
            ["solid-js/store" :refer [createStore]]
            ["@w3t-ab/sqeave" :as sqeave]
            ["./Context.cljs" :refer [AppContext]]
            ["./main.cljs" :refer [Main]])
  (:require-macros [sqeave :refer [defc]]))

(sqeave/init! AppContext)

(defc Root [this {:keys []}]
  (let [[store setStore] (createStore {:click/id {0 {:click/id 0
                                                     :click/count 0}}})]
    #jsx [AppContext.Provider {:value {:store store :setStore setStore}}
          [Main {:& {:ident (fn [] [:click/id 0])}}]]))

(render Root (js/document.getElementById "root"))
