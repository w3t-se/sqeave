(ns comp
  (:require ["solid-js" :as solid :refer [createContext]]
            ["solid-js/store" :refer [createStore]]
            ["./normad.mjs" :as n]
            ["./transact.mjs" :as t]
            ["./utils.mjs" :as u]
            ["loglevel" :as log]
            [squint.core :refer [defclass]]))

(def remotes (atom {}))

(def AppContext nil)

(defn init-ctx! [ctx]
  (log/setLevel "info")
  (let [[store setStore] (createStore {})]
    (set! AppContext ctx)
    (when js/import.meta.env.DEV
      (set! (.-store js/window) store)
      (log/setLevel "debug"))
    {:store store :setStore setStore}))

(defn viewer-ident [this]
  (t/viewer-ident this.ctx))

(defn viewer? [this acc-id]
  (t/viewer? this.ctx acc-id))

(defclass Comp
  (field -ctx)
  (field ctx)
  (^:static field query)
  (field -data)
  (field ident)

  (constructor [this ctx-in]
               (set! -ctx ctx-in))

  Object
  (^:static get-query [_] 1)

  (data [_] -data)
  (-query [this] this.query)
  (render [this ident]))

(defn comp-factory [{:keys [cla body] :as comp} ctx]
  (fn [] )
  #_(let [c (new cla ctx)]
     (.render c body %)))

(defn new-data [this]
  (this.new-data))

(defn mutate! [this mutate-map]
  (let [local (:local mutate-map)
        add (or (:add local) (:add mutate-map))
        remote (:remote mutate-map)
        remove (or (:remove local) (:remove mutate-map))
        opts {:append (:append (or local mutate-map))
              :replace (:replace (or local mutate-map))}]
    #_(when remote
      (if (:query remote)
        (cu/execute-gql-query (:query remote) (:vals remote))))
    (when add
      (log/debug "running add with data: " (this.new-data))
      (t/add! this.ctx (if (= add :new) (this.new-data) add) opts))
    (when remove
      (t/remove-ident! this.ctx (:from mutate-map) remove))))

(defn set!
  ([this ident field event]
   (t/set-field! this.ctx (or (u/e->v event) event) {:replace (conj ident field)}))
  ([this field event]
   (t/set-field! this.ctx (or (u/e->v event) event) {:replace (conj (this.ident) field)})))


(def useContext solid/useContext)
(def pull n/pull)
(def createMemo solid/createMemo)
(def createSignal solid/createSignal)
(def debug log/debug)
