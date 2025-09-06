(ns comp
  (:require ["solid-js" :as solid]
            ["solid-js/web" :as solid-web]
            ["solid-js/store" :refer [createStore]]
            ["./normad.mjs" :as n]
            ["./transact.mjs" :as t]
            ["./utils.mjs" :as u]
            ["consola/browser" :as cb]
            [squint.core :refer [defclass]]))

(def remotes (atom {}))

(def AppContext nil)

#_(def ComponentRegistry (atom {}))

(defn init-ctx! [ctx]
  #_(set! consola.level "info")
  (let [[store setStore] (createStore {})
        [registry setRegistry] (createStore {})]
    (set! AppContext ctx)
    (when (and (.-env js/import.meta)
               (.-DEV js/import.meta.env))
      (set! (.-store js/window) store)
      (set! (.-comps js/window) registry)
      (set! (.-level cb/consola) 4))
    {:store store :setStore setStore :registry registry :setRegistry setRegistry}))

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

  (constructor [this ctx-in ident-in]
               (set! -ctx ctx-in)
               (set! ident ident-in))

  Object
  (^:static get-query [_] 1)

  (data [_] -data)
  (-query [this] this.query)
  (render [this body props]))

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
      (cb/consola.debug "running add with data: " (this.new-data))
      (t/add! this.ctx (if (= add :new) (this.new-data) add) opts))
    (when remove
      (t/remove-ident! this.ctx (:from mutate-map) remove))))

(defn set!
  ([this ident field event]
   (cb/consola.debug "this: " this)
   (t/set-field! this.ctx (or (u/e->v event) event) {:replace (conj ident field)}))
  ([this field event]
   (cb/consola.debug "this: " this)
   (cb/consola.debug "event: "  (or (u/e->v event) event))
   (cb/consola.debug "replace: " (conj this.ident field))
   (t/set-field! this.ctx (or (u/e->v event) event) {:replace (conj this.ident field)})))

(defn remove-nil [my-map]
  (into {} (filter (fn [[k v]] (not (nil? v))) my-map))
  #_(select-keys my-map (keys (filter (comp some? my-map) (keys my-map)))))

(def useContext solid/useContext)
(def pull n/pull)
(def createMemo solid/createMemo)
(def createSignal solid/createSignal)
(def onMount solid/onMount)
(def debug cb/consola.debug)
(def ErrorBoundary solid/ErrorBoundary)
(def createComponent solid-web/createComponent)
(def onCleanup solid/onCleanup)
(def warn cb/consola.warn)
(def consola cb/consola)
(def getOwner solid/getOwner)
(def runWithOwner solid/runWithOwner)
(def remove-nil remove-nil)
