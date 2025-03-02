(ns sqeave)

(defn strip-ns [sym]
  (symbol (name sym)))

(defmacro def-factory [name cla ctx body]
  (list 'defn name ['props] (list 'let ['c (list 'new cla ctx)]
                                  (list '.render 'c body 'props))))

(defmacro defc [name bindings body]
  (let [ntmp (str name)
        n (namespace (first (keys (second bindings))))

        params (first (vals (second bindings)))
        val-vec (mapv #(if (map? %) (first (keys %)) %) params)
        val-vec (mapv strip-ns val-vec)

        keywordify (fn [x] (keyword (str (when n (str n "/")) x)))

        or-map (let [m (-> bindings second :or)]
                 (zipmap (mapv keywordify (keys m)) (vals m)))

        local-map (let [m (-> bindings second :local)]
                    (zipmap (mapv keyword (keys m)) (vals m)))

        query (mapv #(if (map? %)
                       {(keywordify (first (keys %))) (first (vals %))}
                       (keywordify %)) params)

        binding-ctx (:ctx (second bindings))]

    (list 'do
          (list 'defn (symbol (str name "Fn")) [{:keys (conj val-vec 'this 'props 'ctx)}] body)

          (list 'defclass (symbol (str name "Class"))
                (list 'extends 'sqeave/Comp)

                (list (with-meta 'field {:static true}) 'query query)

                (list 'field 'local)
                (list 'field 'set-local!)

                (list 'constructor ['this# 'ctx] (list 'sqeave/debug "constructor: " ntmp " ctx: " 'ctx)
                      (list 'super 'ctx)

                      (list 'set! 'this#.render 'this#.constructor.prototype.render)
                      (list 'set! 'this#.new-data 'this#.constructor.new-data)

                      #_(list 'set! 'this#.render 'this#.constructor.render))

                'Object
                (list (with-meta 'new-data {:static true}) ['_ 'data] (list 'merge or-map 'data))

                (list 'render ['this# 'body 'props]
                      (list 'let [(first bindings) 'this#
                                  '_ (list 'sqeave/debug "render: " ntmp " props: " 'props)
                                  'ctx (list 'or binding-ctx (list `useContext 'this#.-ctx))
                                  'ident (list 'get 'props :ident)
                                  'ident (list 'if-not (list 'fn? 'ident) (list 'fn [] 'ident) 'ident)
                                  'ident (list 'if (list nil? (list 'ident)) (list 'fn [] []) 'ident)
                                  '_ (list 'sqeave/debug ntmp ": p " 'props " i: " (list 'ident) " q: " query " ctx:" 'ctx)
                                  '_ (list 'set! 'this#.ident 'ident)
                                  {:keys ['store 'setStore]} 'ctx
                                  '_ (list 'sqeave/add! 'ctx (list 'this#.new-data))
                                  'data (list 'if (list 'or (list 'and (list 'vector? (list 'ident)) (list '= (list 'count (list 'ident)) 0))
                                                        (list 'sqeave/ident? (list 'ident)))
                                              (list 'do
                                                    (list 'sqeave/createMemo (list 'fn []
                                                                                   (list 'sqeave/debug "memo: " ntmp " ident: " (list 'ident) " query: " query)
                                                                                   (list 'let ['data (list 'sqeave/pull 'store (list 'if (list 'empty? (list 'ident))
                                                                                                                                     'store (list 'ident)) query)]
                                                                                         (list 'sqeave/debug "data: " 'data)
                                                                                         'data))))
                                              (list 'fn [] 'props))
                                  val-vec (mapv #(list 'sqeave/createMemo (list 'fn [] (list % (list 'data)))) (mapv keywordify val-vec))

                                  ['local 'setLocal] (list 'sqeave/createSignal local-map)]
                            (list 'set! 'this#.ctx 'ctx)
                            (list 'set! 'this#.local 'local)
                            (list 'set! 'this#.data 'data)
                            (list 'set! 'this#.set-local! (list 'fn ['this# 'data] (list 'setLocal (list 'merge (list 'local) 'data))))
                            (if local-map
                              (list 'let ['local-map-k (vec (keys local-map))
                                          'local-map-k (mapv #(list 'fn [] (list % (list 'local))) (keys local-map))]
                                    (list 'body (zipmap (mapv keyword (conj val-vec 'this 'props 'ctx)) (conj val-vec 'this# 'props 'ctx))))
                              (list 'body (zipmap (mapv keyword (conj val-vec 'this 'props 'ctx)) (conj val-vec 'this# 'props 'ctx))))
                            #_(list 'if 'children [:<>
                                                   body
                                                   'children] body))))

          (list 'defn name ['props] (list 'let ['c (list 'new (symbol (str name "Class")) 'sqeave/AppContext)]
                                          (list '.render-helper 'c
                                                (list '.render 'c (symbol (str name "Fn")) 'props)))))))
