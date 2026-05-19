(ns sqeave.provider
  (:require ["solid-js" :refer [createContext children]]
            ["solid-js/web" :refer [createComponent]]
            ["./comp.mjs" :as comp]))

(def AppContext (createContext nil))

(defn SqeaveProvider [props]
  (let [ctx (comp/init-ctx! AppContext)]
    #jsx [AppContext.Provider {:value ctx}
          (.-children props)]))
