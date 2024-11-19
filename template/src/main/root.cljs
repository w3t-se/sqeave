(ns app
  (:require ["solid-js" :refer [createContext  createSignal]]
            ["solid-js/store" :refer [createStore]]
            ["sqeave/comp" :as comp]))

(def AppContext (createContext))

(defc Root [this {:keys []}]
  (let [store [store setStore] (createStore {})]
    #jsx [AppContext.Provider {:value ctx}
           [:div "Hello"]]))

(def-factory UiRoot Root AppContext RootFn)

(render UiRoot (js/document.getElementById "root"))
