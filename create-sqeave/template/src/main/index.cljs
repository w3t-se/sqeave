(ns index
  (:require ["solid-js" :as s :refer [createContext]]
            ["solid-js/web" :refer [render]]
            ["@w3t-ab/sqeave" :as sqeave]
            ["./main.cljs" :refer [Main]])
  (:require-macros [sqeave :refer [defc]]))

(def AppContext (createContext))

(sqeave/init-ctx! AppContext)

(defc Root [this {:keys [] :or {} :ctx (sqeave/init-ctx! AppContext)}]
  #jsx [AppContext.Provider {:value this.ctx}
        [Main {:ident [:main/id 0]}]])

#_(let [e (js/document.getElementById "root")]
  (render Root e))


(let [e (js/document.getElementById "root")
      #_ri #_(app/RootClass. (sqeave/init-ctx! sqeave/AppContext))]
  (set! (aget e :innerHTML) "")
  #_(println "ctx: " ri.-ctx)
  #_(render (.render ri app/RootFn) e)
  (render (fn [] #jsx [:div {:class "w-full h-full"}
                       [Root {}]]) e))
