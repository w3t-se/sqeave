(ns comp
  (:require ["solid-js" :as solid]
            ["./normad.mjs" :as n]
            ["./transact.mjs" :as t]
            ;["./composedb/util.cljs" :as cu]
            ["./utils.mjs" :as u]
            [squint.core :refer [defclass]]
            #_["./comp_macro.mjs" :as m])
  #_(:require-macros [comp :refer [defc]]))

(def remotes (atom {}))

(defn set!
  ([this ident field event]
   (t/set-field! this.-ctx (or (u/e->v event) event) {:replace (conj ident field)}))
  ([this field event]
   (t/set-field! this.-ctx (or (u/e->v event) event) {:replace (conj (this.ident) field)})))

(defn viewer-ident [this]
  (t/viewer-ident this.-ctx))

(defn viewer? [this acc-id]
  (t/viewer? this.-ctx acc-id))

(defclass Comp
  (field ctx)
  (^:static field query)
  (field -data)
  (field ident)

  (constructor [this ctx-in]
               (set! ctx ctx-in))

  Object
  (^:static get-query [_] 1)

  #_(^:static new [_ & data] (println "a"))
  #_(ctx [_] -ctx)
  #_(ident [_ id] -ident)
  (data [_] -data)
  (-query [this] this.query)
  #_(-new [this] this.new)
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
      (println "running add with data: " (this.new-data))
      (t/add! this.-ctx (if (= add :new) (this.new-data) add) opts))
    (when remove
      (t/remove-ident! this.-ctx (:from mutate-map) remove))))

(def default Comp)
(def useContext solid/useContext)
(def pull n/pull)
(def createMemo solid/createMemo)
(def createSignal solid/createSignal)
(def ident? u/ident?)
(def uuid u/uuid)
