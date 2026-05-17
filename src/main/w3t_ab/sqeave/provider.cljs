(ns sqeave.provider
  (:require ["solid-js" :refer [createContext]]
            ["./comp.mjs" :as comp]))

(def AppContext (createContext))

(defn SqeaveProvider [{:keys [children]}]
  (let [AppContext (createContext)
        ctx (comp/init-ctx! AppContext)]
    #jsx [AppContext.Provider {:value ctx}
          children]))
