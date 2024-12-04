(ns index
  (:require ["solid-js" :refer [createContext]]
            ["solid-js/web" :refer [render]]
            ["@w3t-ab/sqeave" :as sqeave]
            ["./main.cljs" :refer [Main MainClass]])
  (:require-macros [sqeave :refer [defc]]))

(def AppContext (createContext))

(defc Root [this {:keys [count] :or {count 0} :ctx (sqeave/init-ctx! AppContext)}]
  #jsx [AppContext.Provider {:value this.-ctx}
        [:button {:onClick #(sqeave/set! this :count (inc (count)))} "Plus"]
        [:p {} "Count: " (count)]
        #_[Main {:ident [:main/id 0]}]])

(let [e (js/document.getElementById "root")]
  (set! (aget e :innerHTML) "")
  (render Root e))
