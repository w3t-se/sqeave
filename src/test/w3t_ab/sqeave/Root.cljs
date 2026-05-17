(ns index
  (:require ["solid-js" :refer [createContext createEffect]]
            ["@w3t-ab/sqeave" :as sqeave])
  (:require-macros [sqeave :refer [defc]]))

(def AppContext (createContext))

(defc Root [this {:keys [] :or {} :ctx (sqeave/init-ctx! AppContext)}]
  #jsx [AppContext.Provider {:value ctx}
        [:div {} "I am root."]])
