(ns sqeave)

(defn strip-ns [sym]
  (symbol (name sym)))

(defmacro def-factory [name cla ctx body]
  (list 'defn name ['props] (list 'let ['c (list 'new cla ctx)]
                                  (list '.render 'c body 'props))))

(defn bind-sym-name [x]
  ;; :file/name -> file-name
  ;; dashboard/id -> dashboard-id
  (symbol
   (-> (str x)
       (subs 1)              ;; remove leading :
       (.replace "/" "-"))))

(defn bindable? [x]
  (symbol? x))

(defn pull-leaf [x]
  (cond
    (symbol? x)  (keyword (str x))
    (keyword? x) x
    (string? x)  x
    :else        x))

(defn sym->kw [s]
  (keyword
   (if-let [ns (namespace s)]
     (str ns "/" (name s))
     (name s))))

(defn sym->binding [s]
  (symbol
   (if-let [ns (namespace s)]
     (str ns "-" (name s))
     (name s))))

(defn single-entry [m]
  (let [s (seq m)]
    (when-not (= 1 (count s))
      (throw (js/Error. (str ":more map entries must contain exactly one entry: " (pr-str m)))))
    (first s)))

(defn parse-more-query
  ([q] (parse-more-query [] q))
  ([path q]
   (cond
     (vector? q)
     (let [parsed (mapv #(parse-more-query path %) q)]
       {:query    (mapv :query parsed)
        :bindings (vec (mapcat :bindings parsed))})

     (map? q)
     (let [entry   (single-entry q)
           k       (key entry)
           v       (val entry)
           k*      (pull-leaf k)
           parsed  (parse-more-query (conj path k*) v)]
       {:query    {k* (:query parsed)}
        :bindings (:bindings parsed)})

     (symbol? q)
     (let [kw (sym->kw q)]
       {:query kw
        :bindings [{:sym  (sym->binding q)
                    :path (conj path kw)}]})

     :else
     {:query q
      :bindings []})))

(defmacro defc [name bindings body]
  (let [ntmp (str name)
        n (namespace (first (keys (second bindings))))

        params (first (vals (second bindings)))
        val-vec (mapv #(if (map? %) (first (keys %)) %) params)
        val-vec (mapv strip-ns val-vec)

        keywordify (fn [x] (keyword (str (when n (str n "/")) x)))

        val-keys (mapv keyword val-vec)

        or-map (let [m (-> bindings second :or)
                     m (if (fn? m) (m) m)]
                 (zipmap (mapv keywordify (keys m)) (vals m)))

        local-map (let [m (-> bindings second :local)]
                    (zipmap (mapv keyword (keys m)) (vals m)))

        query (mapv #(if (map? %)
                       {(keywordify (first (keys %))) (first (vals %))}
                       (keywordify %)) params)

        binding-ctx (:ctx (second bindings))

        more-raw      (-> bindings second :more)
        more-parsed   (parse-more-query more-raw)
        more-query    (:query more-parsed)
        more-bindings (:bindings more-parsed)

        more-syms     (mapv :sym more-bindings)
        more-keys     (mapv keyword more-syms)

        ]
    (list 'do
      (list 'defn (symbol (str name "Fn")) [{:keys (vec (concat val-vec ['more] more-syms ['this 'props 'ctx]))}]

            (list 'squint-compiler-jsx
                  ['sqeave/ErrorBoundary {:fallback (list 'fn ['err 'reset]
                                                          (list 'sqeave/warn 'err)
                                                          (list 'sqeave/onMount (list 'fn []
                                                                                      #_`(when (some? (.-hot js/import.meta))
                                                                                           (.accept (.-hot js/import.meta)
                                                                                                    (fn []
                                                                                                      ~(list 'reset)
                                                                                                      (sqeave/debug "🔄 Hot Reload detected!"))))))
                                                          (list 'squint-compiler-jsx
                                                                [:div {:onClick (list 'fn ['e] (list 'reset))}
                                                                 (list :message 'err)]))}
                   #_(list 'sqeave/onMount (list 'fn []
                                                 (list 'sqeave/debug "I was mounted: " (list 'this.data) ": " (str name "Fn") " " 'this.ident)
                                                 (list 'sqeave/debug "owner1:" (list 'sqeave/getOwner))))
                   #_(list 'sqeave/onCleanup (list 'fn []
                                                   #_(list 'setRegistry (list 'update 'registry (str name "Class") 'dissoc (list 'second 'this.ident)))
                                                   (list 'sqeave/debug "I was cleaned up: " (str name "Fn") " " 'this.ident " " (list 'sqeave/getOwner))))
                   body]))

      (list 'defclass (symbol (str name "Class"))
            (list 'extends 'sqeave/Comp)

            (list (with-meta 'field {:static true}) 'query query)

            (list 'field 'local)
            (list 'field 'set-local!)

            (list 'constructor ['this# 'ctx 'ident] (list 'sqeave/debug "constructor: " ntmp " ctx: " 'ctx " ident: " 'ident)
                  (list 'super 'ctx 'ident)
                  (list 'set! 'this#.ident 'ident)
                  (list 'set! 'this#.render 'this#.constructor.prototype.render)
                  (list 'set! 'this#.new-data 'this#.constructor.new-data))

            'Object
            (list (with-meta 'new-data {:static true}) ['_ 'data] (list 'merge or-map 'data))

            (list 'first-render ['this# 'props]
                  (list 'let [(first bindings) 'this#
                              '_ (list 'sqeave/debug "render: " ntmp " props: " 'props)
                              'ctx (list 'or
                                         binding-ctx
                                         (list 'if 'this#.-ctx
                                               (list `useContext 'this#.-ctx)
                                               (list 'throw
                                                     (list 'js/Error.
                                                           (str ntmp " has nil AppContext")))))

                                        ;'_ (list 'sqeave/debug ntmp ": p " 'props " i: " 'this#.ident " q: " query " ctx:" 'ctx " exists:" (list 'js/Reflect.has (list 'get 'ctx :store) (list 'first 'this#.ident)))
                                        ;['force 'setForce] (list 'sqeave/createSignal false)
                                        ;'_ (list 'set! 'this#.ident 'ident)
                              {:keys ['store 'setStore]} 'ctx

                              #_'data #_(list 'sqeave/remove-nil (list 'sqeave/pull (list 'get 'ctx :store) (list 'if (list 'empty? 'this#.ident)
                                                                                                                  (list 'get 'ctx :store) 'this#.ident)
                                                                       query))

                              '_ (list 'sqeave/debug "store: " ntmp " " (list 'get 'ctx :store))
                              '_ (list 'sqeave/add! 'ctx (list 'this#.new-data (list 'if-not (list 'nil? (list 'second 'this#.ident))
                                                                                     {(list 'first 'this#.ident) (list 'second 'this#.ident)}
                                                                                     {})))
                                  
                              #_'_ #_(list 'if-not (list 'js/Reflect.has (list 'get 'ct :store) (list 'first 'this#.ident))
                                           (list 'sqeave/add! 'ctx (list 'this#.new-data (list 'if-not (list 'nil? (list 'second 'this#.ident))
                                                                                               {(list 'first 'this#.ident) (list 'second 'this#.ident)})))
                                           (list 'if-not (list 'js/Reflect.has (list 'get-in 'ctx [:store (list 'first 'this#.ident)]) (list 'second 'this#.ident))
                                                 (list 'sqeave/add! 'ctx (list 'this#.new-data (list 'if-not (list 'nil? (list 'second 'this#.ident))
                                                                                                     {(list 'first 'this#.ident) (list 'second 'this#.ident)})))))


                              #_'data #_(list 'if-not (list 'empty? query)
                                              (list 'let ['data (list 'sqeave/createMemo (list 'fn []
                                                                                               #_(list 'force)
                                                                                               (list 'sqeave/debug "memo: " ntmp " ident: " 'this#.ident " query: " query)
                                                                                               (list 'let ['data (list 'sqeave/pull (list 'get 'ctx :store) (list 'if (list 'empty? 'this#.ident)
                                                                                                                                                                  (list 'get 'ctx :store) 'this#.ident) query)]
                                                                                                     (list 'sqeave/debug "memo: " ntmp " ident: " 'this#.ident "data: " 'data)
                                                                                                     'data)))]

                                                    #_(list 'sqeave/debug "nn:" (list 'data) ":" (list 'sqeave/remove-nil (list 'data)))

                                                    'data)
                                              (list 'fn [] 'props))
                              'val-v (list 'mapv (list 'fn ['x]
                                                       (list 'if-not (list 'map? 'x)
                                                             (list 'sqeave/createMemo (list 'fn [] (list 'get-in 'ctx [:store (list 'first 'this#.ident) (list 'second 'this#.ident) 'x])))
                                                             (list 'sqeave/createMemo (list 'fn []
                                                                                            (list 'sqeave/pull (list 'get 'ctx :store)
                                                                                                  [(list 'first 'this#.ident) (list 'second 'this#.ident) (list 'first (list 'keys 'x))]
                                                                                                  (list 'first (list 'vals  'x)))))))
                                           query #_(mapv keywordify val-vec))
                              val-vec 'val-v
                              ['local 'setLocal] (list 'sqeave/createSignal local-map)
                              'local-map-k (vec (keys local-map))
                              'local-map-k (mapv #(list 'fn [] (list % (list 'this#.local))) (keys local-map))

                              'more
                              (list 'sqeave/createMemo
                                    (list 'fn []
                                          (list 'sqeave/pull
                                                (list 'get 'ctx :store)
                                                (list 'get 'ctx :store)
                                                more-query)))

                              'more-v
                              (apply list
                                     'vector
                                     (map
                                      (fn [{:keys [path]}]
                                        (list 'sqeave/createMemo
                                              (list 'fn []
                                                    (list 'get-in (list 'more) path))))
                                      more-bindings))]

                        (list 'set! 'this#.ctx 'ctx)
                        #_(list 'set! 'this#.force 'force)
                        #_(list 'set! 'this#.setForce 'setForce)
                        (list 'set! 'this#.local 'local)
                        #_(list 'set! 'this#.data 'data)
                        (list 'set! 'this#.val-vec 'val-v)
                        (list 'set! 'this#.set-local! (list 'fn ['this# 'data] (list 'setLocal (list 'merge (list 'local) 'data))))

                        (list 'squint-compiler-jsx
                              (list (symbol (str name "Fn"))
                                    (list 'zipmap
                                          (vec (concat val-keys [:more] more-keys [:this :props :ctx]))
                                          (list 'concat
                                                (vec (concat val-vec ['more]))
                                                'more-v
                                                (list 'vector 'this# 'props 'ctx)))
                                    #_(list 'zipmap
                                            (list 'concat val-keys [:more] more-keys [:this :props :ctx])
                                            (list 'concat val-vec (list 'vector 'more) 'more-v (list 'vector 'this# 'props 'ctx)))
                                    #_(list 'zipmap
                                            (list 'conj val-keys :this :props :ctx)
                                            (list 'conj val-vec 'this# 'props 'ctx))))))

            (list 'render ['this# 'body 'props]
                  #_(list 'this#.setForce (list 'not (list 'this#.force)))
                  #_(list  'this#.val-vec val-keys)
                  (list 'sqeave/debug "this:data" (list 'this#.data))
                  (list 'let ['local-map-k (vec (keys local-map))
                              'local-map-k (mapv #(list 'fn [] (list % (list 'this#.local))) (keys local-map))]
                        (list 'body (list 'zipmap (list 'conj val-keys :this :props :ctx) (list 'conj 'this#.val-vec 'this# 'props 'this#.ctx))))))

      (list 'defn (symbol (str name "Factory")) ['ident 'props]
            (list 'let [ ;'ctx (list 'or binding-ctx (list `useContext 'sqeave/AppContext))
                                        ;'setRegistry (list 'get 'ctx :setRegistry)
                                        ;'registry (list 'get 'ctx :registry)
                                        ;'owner (list 'if-not binding-ctx (list 'sqeave/getOwner))
                        ]
                  #_(list 'swap! 'sqeave/ComponentRegistry 'assoc-in [(str name "Class") (list 'second 'ident)] 'c)

                  #_(when (.-hot js/import.meta)
                      (.accept (.-hot js/import.meta) (fn []
                                                        (list '.render 'c (symbol (str name "Fn")) 'props))))

                  (list 'let ['c (list 'new (symbol (str name "Class")) 'sqeave/AppContext 'ident)]
                        #_(list 'set! 'this#.owner 'owner)
                        #_(list 'setRegistry (list 'fn [] (list 'assoc-in 'registry [(str name "Class") (list 'second 'ident)] 'c)))
                        (list '.first-render 'c 'props)
                        #_(list '.render 'c (symbol (str name "Fn")) 'props)
                        #_(list 'let ['c (list 'get-in 'registry #_(list 'deref 'sqeave/ComponentRegistry) [(list 'str name "Class") (list 'second 'ident)])]
                                (list '.render 'c (symbol (str name "Fn")) 'props)))))

      #_(list 'defn name ['props]
              `(let [ident (get props :ident)
                     ident (if (fn? ident) (ident) ident)]
                 (sqeave/debug "idnet: " ident)
                 (when-not (deref sqeave/HOT_LOADED)
                   (if-let [c (get-in (deref sqeave/ComponentRegistry)
                                      [(str ~name "Class") (second ident)])]
                     (do (sqeave/debug "class: " c) (.render c ~(symbol (str name "Fn")) props))
                     (~(symbol (str name "Factory")) ident props)))))

      #_(defonce ~(symbol "HOT_LOADED") (atom false))

      #_(when (.-hot js/import.meta)
          (.accept (.-hot js/import.meta) (fn []
                                            (println "hot")
                                            (reset! ~(symbol "HOT_LOADED") true))))

      (list 'defn name ['props]
            (list 'let ['ident (list 'get 'props :ident)
                        'ident (list 'if-not (list 'fn? 'ident) 'ident (list 'ident))
                                        ;'ctx (list 'or binding-ctx (list `useContext 'sqeave/AppContext))
                                        ;'_ (list 'println 'ctx)
                                        ;'registry (list 'get 'ctx :registry)
                                        ;'owner (list 'if-not binding-ctx (list 'sqeave/getOwner))
                        ]
                  #_(list 'sqeave/debug "idnet: " 'ident " owner: " 'owner)

                  (list (symbol (str name "Factory")) 'ident 'props)

                  ;; use if comp registry
                  #_(list 'if-let ['c (list 'get-in 'registry [(str name "Class") (list 'second 'ident)])]
                          (list 'do
                                (list 'aset 'c :ctx 'ctx)
                                (list 'sqeave/debug "class: " 'c)
                                (list 'sqeave/runWithOwner 'owner
                                      (list 'fn [] (list '.render 'c (symbol (str name "Fn")) 'props))))
                          (list (symbol (str name "Factory")) 'ident 'props)))))))

;list 's/runWithOwner 'owner
