(ns transact
  (:require ["./normad.mjs" :as n]
            ["./utils.mjs" :as u]
            ["consola/browser" :refer [consola]]))

#_(defn rec-add [path value]
  (loop [p (first path)
         r (rest path)]
    (into {} [p (if (nil? (first r))
                  value
                  (recur (first r) (rest r)))])))

(defn alert-error [ctx error]
  (n/add ctx {:component/id :alert
             :title "Error"
             :visible? true
             :type :error
             :interval 4000
             :message (str error)} {}))

(defn get-viewer-user [{:keys [store setStore] :as ctx}]
  (n/pull store [:viewer/id 0] [{:viewer/user [:user/id :user/session :user/account]}]))

(defn viewer-ident [ctx]
  (-> (get-viewer-user ctx) :viewer/user :user/account))

(defn viewer? [ctx acc]
  (let [viewer-id (second (viewer-ident ctx))]
    (and (= viewer-id acc) (not (nil? viewer-id)) (not (nil? acc)))))

(defn check-session [{:keys [store setStore] :as ctx}]
  (if-not (get-in (get-viewer-user ctx) [:viewer/user :user/session])
    (throw (js/Error. "Sign in to to make changes."))))

(defn wrap-session [ctx check-session? f]
  (try
    (if check-session?
      (check-session ctx))
    (f)
    (catch js/Error e
      (alert-error ctx e)
      (consola.error e))))

(defn set-field! [{:keys [store setStore] :as ctx} value {:keys [append replace check-session?] :or {append false replace false check-session? false}  :as param}]
  (wrap-session ctx check-session?
                (fn []
                  (let [path (or append replace)
                        action (if append
                                 (if replace
                                   #(u/distinct-second-elements (conj % value))
                                   #(conj % value))
                                 #(identity value))]
                    (apply setStore (conj path (fn [x] (action x))))
                    #_(when (u/uuid? (second ident))
                        (consola.debug "uuid:" ident)
                        (setStore (first ident) (second ident)
                                  (fn [x]
                                    (let [p (or (:uuid/paths x) [])]
                                      (assoc x :uuid/paths (conj p path))))))))))

(defn add-ident! [{:keys [store setStore] :as ctx} ident {:keys [append replace check-session?] :or {append false replace false check-session? false} :as param}]
  (set-field! ctx ident param))

(defn remove-ident! [{:keys [store setStore] :as ctx} path ident {:keys [check-session?] :or {check-session? false}}]
  (wrap-session ctx check-session?
                (fn []
                  (apply setStore (conj path (fn [x] (try
                                                       (u/remove-ident ident x)
                                                       (catch js/Error e
                                                         (consola.error e)
                                                         x))))))))

(defn add! [{:keys [store setStore] :as ctx} value {:keys [append replace after check-session?] :or {append false replace false after
                                                                                                     false check-session? false} :as params}]
  (wrap-session ctx check-session?
                #(let [res (n/add ctx value)]
                   (if (or append replace)
                     (add-ident! ctx res params))
                   (if after
                     (after)))))

(defn remove-entity! [])

(defn swap-uuids! [{:keys [store setStore] :as ctx} ident stream-id]
  (let [n1 (first ident)
        new-ident [n1 stream-id]
        obj (get-in store ident)
        new-obj (assoc obj n1 stream-id)
        paths (get obj :uuid/paths)]
    (setStore (first ident) (fn [x]
                              (assoc x stream-id new-obj)))
    (consola.debug "path: " paths)
    (mapv #(apply setStore (conj % (fn [x]
                                     (if (u/ident? x)
                                       new-ident
                                       (if (vector? x)
                                         (conj (u/remove-ident ident x) new-ident)))))) paths)))
