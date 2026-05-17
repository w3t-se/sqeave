(ns sqeave.provider
  (:require ["solid-js" :refer [createContext]]
            ["./comp.cljs" :as comp]))

(def AppContext (createContext))

(defn SqeaveProvider [{:keys [children devtools?]}]
  (let [ctx (comp/init-ctx! AppContext)]
    #jsx
    [AppContext.Provider {:value ctx}
     children]))
