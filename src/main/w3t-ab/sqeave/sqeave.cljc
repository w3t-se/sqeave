(ns sqeave)

(defn strip-ns [sym]
  (symbol (name sym)))

(defmacro def-factory [name cla ctx body]
  `(defn ~name [props]
     (let [c (new ~cla ~ctx)]
       (.render c ~body props))))

(defmacro defc [name bindings body]
  (let [ntmp (str name)
        n (namespace (first (keys (second bindings))))
        params (first (vals (second bindings)))
        val-vec (mapv #(if (map? %) (first (keys %)) %) params)
        val-vec (mapv strip-ns val-vec)
        keywordify (fn [x] (keyword (str (when n (str n "/")) x)))
        val-keys (mapv keyword val-vec)
        or-map (let [m (-> bindings second :or)]
                 (zipmap (mapv keywordify (keys m)) (vals m)))
        local-map (let [m (-> bindings second :local)]
                    (zipmap (mapv keyword (keys m)) (vals m)))
        query (mapv #(if (map? %)
                       {(keywordify (first (keys %))) (first (vals %))}
                       (keywordify %))
                    params)
        binding-ctx (:ctx (second bindings))
        fn-name (symbol (str name "Fn"))
        class-name (symbol (str name "Class"))
        factory-name (symbol (str name "Factory"))
        zip-args `(zipmap ~(conj val-keys :this :props :ctx)
                          ~(conj 'val-vec 'this# 'props 'ctx))]
    `(do
       (defn ~fn-name [{:keys ~(conj val-vec 'this 'props 'ctx)}]
         (squint-compiler-jsx
          ['sqeave/ErrorBoundary
           {:fallback (fn [err reset]
                        (sqeave/warn err)
                        (sqeave/onMount (fn []))
                        (squint-compiler-jsx
                         [:div {:onClick (fn [_e] (reset))}
                          (:message err)]))}
           ~body]))

       (defclass ~class-name
         (extends sqeave/Comp)
         ~(with-meta 'field {:static true}) query ~query
         field local
         field set-local!
         (constructor [this# ctx ident]
           (sqeave/debug "constructor: " ~ntmp " ctx: " ctx " ident: " ident)
           (super ctx ident)
           (set! this#.ident ident)
           (set! this#.render this#.constructor.prototype.render)
           (set! this#.new-data this#.constructor.new-data))

         Object
         ~(with-meta 'new-data {:static true}) [_ data] (merge ~or-map data)

         (first-render [this# props]
           (let [~(first bindings) this#
                 ident$ (sqeave/createMemo
                         (fn []
                           (let [i (get props :ident)]
                             (if (fn? i) (i) i))))
                 _ (set! this#.ident ident$)
                 _ (sqeave/debug "render: " ~ntmp " props: " props)
                 ctx (or ~binding-ctx (sqeave/useContext this#.-ctx))
                 _ (sqeave/debug ~ntmp ": p " props " i: " (this#.ident) " q: " ~query " ctx:" ctx
                                 " exists:" (js/Reflect.has (get ctx :store) (first (this#.ident))))
                 {:keys [store setStore]} ctx
                 val-v (mapv (fn [x]
                               (if-not (map? x)
                                 (sqeave/createMemo (fn [] (get-in ctx [:store (first (this#.ident)) (second (this#.ident)) x])))
                                 (sqeave/createMemo (fn []
                                                      (sqeave/pull (get ctx :store)
                                                                   [(first (this#.ident)) (second (this#.ident)) (first (keys x))]
                                                                   (first (vals x)))))))
                             ~query)
                 val-vec val-v
                 [local setLocal] (sqeave/createSignal ~local-map)
                 local-map-k (vec (keys ~local-map))
                 local-map-k (mapv #(fn [] (~% (this#.local))) (keys ~local-map))]
             (set! this#.ctx ctx)
             (set! this#.local local)
             (set! this#.val-vec val-v)
             (set! this#.set-local! (fn [_this# data] (setLocal (merge local data))))
             (sqeave/debug "thiss: " this# "ctc: " ctx " m: " (zipmap (conj ~val-keys :this :props :ctx) (conj val-vec this# props ctx)))
             (squint-compiler-jsx
              (~fn-name ~zip-args))))

         (render [this# body props]
           (let [local-map-k (vec (keys ~local-map))
                 local-map-k (mapv #(fn [] (~% (this#.local))) (keys ~local-map))]
             (body (zipmap (conj ~val-keys :this :props :ctx) (conj this#.val-vec this# props this#.ctx))))))

       (defn ~factory-name [ident props]
         (let [c (new ~class-name sqeave/AppContext ident)]
           (.first-render c props)))

       (defn ~name [props]
         (let [ident (get props :ident)
               ident (if-not (fn? ident) ident (ident))]
           (~factory-name ident props))))))

;list 's/runWithOwner 'owner
