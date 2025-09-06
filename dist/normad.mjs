import * as squint_core from 'squint-cljs/core.js';
import * as u from './utils.mjs';
import { consola } from 'consola/browser';
import { reconcile } from 'solid-js/store';
import { batch } from 'solid-js';
var id_suffix = "/id";
var ident_QMARK_ = function (x) {
const and__23227__auto__1 = squint_core.vector_QMARK_(x);
if (squint_core.truth_(and__23227__auto__1)) {
const and__23227__auto__2 = (squint_core.count(x)) === (2);
if (and__23227__auto__2) {
const k3 = squint_core.first(x);
const v4 = squint_core.second(x);
const and__23227__auto__5 = squint_core.string_QMARK_(k3);
if (squint_core.truth_(and__23227__auto__5)) {
const and__23227__auto__6 = k3.endsWith(id_suffix);
if (squint_core.truth_(and__23227__auto__6)) {
const or__23193__auto__7 = squint_core.number_QMARK_(v4);
if (squint_core.truth_(or__23193__auto__7)) {
return or__23193__auto__7} else {
const or__23193__auto__8 = squint_core.string_QMARK_(v4);
if (squint_core.truth_(or__23193__auto__8)) {
return or__23193__auto__8} else {
return (void 0 === v4)};
};
} else {
return and__23227__auto__6};
} else {
return and__23227__auto__5};
} else {
return and__23227__auto__2};
} else {
return and__23227__auto__1};

};
var traverse_and_transform = function (item, setStore) {
if (squint_core.truth_(squint_core.vector_QMARK_(item))) {
return squint_core.mapv((function (_PERCENT_1) {
return traverse_and_transform(_PERCENT_1, setStore);

}), item)} else {
if (squint_core.truth_(squint_core.map_QMARK_(item))) {
const ident1 = u.get_ident(item);
const new_val2 = squint_core.zipmap(squint_core.keys(item), squint_core.mapv((function (_PERCENT_1) {
return traverse_and_transform(_PERCENT_1, setStore);

}), squint_core.vals(item)));
if (squint_core.truth_(ident_QMARK_(ident1))) {
squint_core.swap_BANG_(setStore, (function (_PERCENT_1) {
return squint_core.update_in(_PERCENT_1, ident1, (function (v) {
return squint_core.merge(v, new_val2);

}));

}));
return ident1;
} else {
return new_val2};
} else {
if ("else") {
return item} else {
return null}}};

};
var normalize_STAR_ = function (x) {
const acc1 = squint_core.js_obj();
const put_BANG_2 = (function (table, id, m) {
const id4 = squint_core.str(id);
const tbl5 = (() => {
const or__23193__auto__6 = acc1[table];
if (squint_core.truth_(or__23193__auto__6)) {
return or__23193__auto__6} else {
return squint_core.js_obj()};

})();
const prev7 = tbl5[id4];
const row8 = Object.assign(squint_core.js_obj(), prev7, m);
(tbl5[id4] = row8);
return (acc1[table] = tbl5);

});
const walk3 = (function (v) {
if (squint_core.truth_(squint_core.vector_QMARK_(v))) {
return squint_core.mapv(walk3, v)} else {
if (squint_core.truth_(squint_core.map_QMARK_(v))) {
const ident9 = u.get_ident(v);
const m10 = squint_core.reduce_kv((function (o, k, val) {
return squint_core.assoc(o, k, walk3(val));

}), ({  }), v);
if (squint_core.truth_((() => {
const and__23227__auto__11 = squint_core.vector_QMARK_(ident9);
if (squint_core.truth_(and__23227__auto__11)) {
return (2) === (squint_core.count(ident9))} else {
return and__23227__auto__11};

})())) {
put_BANG_2(squint_core.first(ident9), squint_core.second(ident9), m10);
return ident9;
} else {
return m10};
} else {
if ("else") {
return v} else {
return null}}};

});
const root_STAR_12 = walk3(x);
return [acc1, root_STAR_12];

};
var add = (() => {
const f6 = (function (var_args) {
const args71 = [];
const len__23152__auto__2 = arguments.length;
let i83 = 0;
while(true){
if ((i83) < (len__23152__auto__2)) {
args71.push((arguments[i83]));
let G__4 = (i83 + 1);
i83 = G__4;
continue;
};break;
}
;
const argseq__23356__auto__5 = (((1) < (args71.length)) ? (args71.slice(1)) : (null));
return f6.cljs$core$IFn$_invoke$arity$variadic((arguments[0]), argseq__23356__auto__5);

});
f6.cljs$core$IFn$_invoke$arity$variadic = (function (p__10, data) {
const map__67 = p__10;
const ctx8 = map__67;
const setStore9 = squint_core.get(map__67, "setStore");
const store10 = squint_core.get(map__67, "store");
const temp__22828__auto__11 = squint_core.first(data);
if (squint_core.truth_(temp__22828__auto__11)) {
const input12 = temp__22828__auto__11;
const vec__1316 = normalize_STAR_(input12);
const acc17 = squint_core.nth(vec__1316, 0, null);
const root_STAR_18 = squint_core.nth(vec__1316, 1, null);
batch((function () {
for (let G__19 of squint_core.iterable(Object.keys(acc17))) {
const t20 = G__19;
const rows21 = acc17[t20];
setStore9(t20, (function (tbl) {
const or__23193__auto__22 = tbl;
if (squint_core.truth_(or__23193__auto__22)) {
return or__23193__auto__22} else {
return ({  })};

}));
for (let G__23 of squint_core.iterable(Object.keys(rows21))) {
const id24 = G__23;
const row25 = rows21[id24];
setStore9(t20, id24, reconcile(row25, ({ "merge": true })))
}
}return null;

}));
return root_STAR_18;
};

});
f6.cljs$lang$maxFixedArity = 1;
return f6;

})();
var acc = squint_core.atom(({  }));
var resolve_ident = function (st, ident) {
if (squint_core.truth_(ident_QMARK_(ident))) {
return squint_core.get_in(st, ident)} else {
return ident};

};
var pull_one = function (st, entity, query) {
if ((entity == null)) {
return null} else {
if (squint_core.truth_(ident_QMARK_(entity))) {
return pull_one(st, squint_core.get_in(st, entity), query)} else {
if (squint_core.truth_(squint_core.vector_QMARK_(entity))) {
return squint_core.mapv((function (_PERCENT_1) {
if (squint_core.truth_(ident_QMARK_(_PERCENT_1))) {
return pull_one(st, _PERCENT_1, query)} else {
return _PERCENT_1};

}), entity)} else {
if (squint_core.truth_(squint_core.vector_QMARK_(query))) {
const simple1 = squint_core.filterv(squint_core.string_QMARK_, query);
const nested2 = squint_core.filterv((function (_PERCENT_1) {
return squint_core.map_QMARK_(_PERCENT_1);

}), query);
const base3 = squint_core.into(({  }), squint_core.map((function (k) {
return [k, pull_one(st, entity, k)];

}), simple1));
return squint_core.reduce((function (m, mquery) {
const k4 = squint_core.first(squint_core.keys(mquery));
const subq5 = squint_core.get(mquery, k4);
const v6 = squint_core.get(entity, k4);
if ((v6 == null)) {
return m} else {
return squint_core.assoc(m, k4, ((squint_core.truth_(ident_QMARK_(v6))) ? (pull_one(st, v6, subq5)) : (squint_core.mapv((function (_PERCENT_1) {
return pull_one(st, _PERCENT_1, subq5);

}), v6))))};

}), base3, nested2);
} else {
if (squint_core.truth_(squint_core.map_QMARK_(query))) {
const nk7 = squint_core.first(squint_core.keys(query));
const subq8 = squint_core.get(query, nk7);
const temp__22828__auto__9 = squint_core.get(entity, nk7);
if (squint_core.truth_(temp__22828__auto__9)) {
const data10 = temp__22828__auto__9;
const G__1111 = ({  });
(G__1111[nk7] = ((squint_core.truth_(ident_QMARK_(data10))) ? (pull(store, squint_core.get_in(store, data10), subq8)) : (((squint_core.truth_(squint_core.vector_QMARK_(data10))) ? (squint_core.mapv((function (_PERCENT_1) {
if (squint_core.truth_(ident_QMARK_(_PERCENT_1))) {
return pull(store, squint_core.get_in(store, _PERCENT_1), subq8)} else {
return pull(store, _PERCENT_1, subq8)};

}), data10)) : ((("else") ? (pull(store, data10, subq8)) : (null)))))));
return G__1111;
};
} else {
if (squint_core.truth_(squint_core.string_QMARK_(query))) {
return squint_core.get(entity, query)} else {
if ("else") {
return entity} else {
return null}}}}}}};

};
var id_suffix = "/id";
var ident_QMARK_ = function (x) {
const and__23227__auto__1 = squint_core.vector_QMARK_(x);
if (squint_core.truth_(and__23227__auto__1)) {
const and__23227__auto__2 = (squint_core.count(x)) === (2);
if (and__23227__auto__2) {
const k3 = x[0];
const and__23227__auto__4 = squint_core.string_QMARK_(k3);
if (squint_core.truth_(and__23227__auto__4)) {
return k3.endsWith(id_suffix)} else {
return and__23227__auto__4};
} else {
return and__23227__auto__2};
} else {
return and__23227__auto__1};

};
var resolve_entity = function (store, entity) {
if ((entity == null)) {
return null} else {
if (squint_core.truth_((() => {
const and__23227__auto__1 = squint_core.vector_QMARK_(entity);
if (squint_core.truth_(and__23227__auto__1)) {
const and__23227__auto__2 = (squint_core.count(entity)) >= (2);
if (and__23227__auto__2) {
const k3 = entity[0];
const and__23227__auto__4 = squint_core.string_QMARK_(k3);
if (squint_core.truth_(and__23227__auto__4)) {
return k3.endsWith(id_suffix)} else {
return and__23227__auto__4};
} else {
return and__23227__auto__2};
} else {
return and__23227__auto__1};

})())) {
const tbl5 = entity[0];
const id6 = entity[1];
const base7 = squint_core.get_in(store, [tbl5, id6]);
const rest8 = (((entity.length) > (2)) ? (entity.slice(2)) : (null));
if (squint_core.truth_((() => {
const and__23227__auto__9 = rest8;
if (squint_core.truth_(and__23227__auto__9)) {
return (rest8.length) > (0)} else {
return and__23227__auto__9};

})())) {
return squint_core.get_in(base7, rest8)} else {
return base7};
} else {
if (squint_core.truth_(ident_QMARK_(entity))) {
return squint_core.get_in(store, entity)} else {
if ("else") {
return entity} else {
return null}}}};

};
var pull = function (store, entity, query) {
const e1 = resolve_entity(store, entity);
if ((e1 == null)) {
return null} else {
if (squint_core.truth_(ident_QMARK_(e1))) {
return pull(store, squint_core.get_in(store, e1), query)} else {
if (squint_core.truth_((() => {
const and__23227__auto__2 = squint_core.vector_QMARK_(e1);
if (squint_core.truth_(and__23227__auto__2)) {
return (e1.length > 0)} else {
return and__23227__auto__2};

})())) {
return squint_core.mapv((function (it) {
if (squint_core.truth_(ident_QMARK_(it))) {
return pull(store, it, query)} else {
return pull(store, it, query)};

}), e1)} else {
if (squint_core.truth_(squint_core.vector_QMARK_(query))) {
const simple3 = squint_core.vec(squint_core.filter(squint_core.string_QMARK_, query));
const nested4 = squint_core.vec(squint_core.filter((function (x) {
return squint_core.not(squint_core.string_QMARK_(x));

}), query));
const out5 = squint_core.reduce((function (m, k) {
return squint_core.assoc(m, k, squint_core.get(e1, k));

}), ({  }), simple3);
return squint_core.reduce((function (m, mquery) {
const k6 = squint_core.first(Object.keys(mquery));
const sub7 = mquery[k6];
const v8 = squint_core.get(e1, k6);
if ((v8 == null)) {
return m} else {
return squint_core.assoc(m, k6, ((squint_core.truth_(ident_QMARK_(v8))) ? (pull(store, v8, sub7)) : (((squint_core.truth_(squint_core.vector_QMARK_(v8))) ? (squint_core.mapv((function (vv) {
if (squint_core.truth_(ident_QMARK_(vv))) {
return pull(store, vv, sub7)} else {
return pull(store, vv, sub7)};

}), v8)) : ((("else") ? (pull(store, v8, sub7)) : (null)))))))};

}), out5, nested4);
} else {
if (squint_core.truth_((() => {
const and__23227__auto__9 = squint_core.map_QMARK_(query);
if (squint_core.truth_(and__23227__auto__9)) {
return (Object.keys(query).length > 0)} else {
return and__23227__auto__9};

})())) {
const k10 = squint_core.first(Object.keys(query));
const sub11 = query[k10];
const v12 = squint_core.get(e1, k10);
if (squint_core.truth_(squint_core.some_QMARK_(v12))) {
const G__1213 = ({  });
(G__1213[k10] = ((squint_core.truth_(ident_QMARK_(v12))) ? (pull(store, v12, sub11)) : (((squint_core.truth_(squint_core.vector_QMARK_(v12))) ? (squint_core.mapv((function (vv) {
if (squint_core.truth_(ident_QMARK_(vv))) {
return pull(store, vv, sub11)} else {
return pull(store, vv, sub11)};

}), v12)) : ((("else") ? (pull(store, v12, sub11)) : (null)))))));
return G__1213;
};
} else {
if (squint_core.truth_(squint_core.string_QMARK_(query))) {
return squint_core.get(e1, query)} else {
if ("else") {
return e1} else {
return null}}}}}}};

};
var update_uuid_in_coll = function (coll, old_uuid, new_uuid) {
return squint_core.mapv((function (item) {
if (squint_core.truth_(squint_core.map_QMARK_(item))) {
return update_uuid_in_map(item, old_uuid, new_uuid)} else {
if (squint_core.truth_(squint_core.vector_QMARK_(item))) {
if ((squint_core.second(item)) === (old_uuid)) {
return squint_core.assoc(item, 1, new_uuid)} else {
return update_uuid_in_coll(item, old_uuid, new_uuid)}} else {
if ("else") {
return item} else {
return null}}};

}), coll);

};
var update_uuid_in_map = function (m, old_uuid, new_uuid) {
return squint_core.reduce_kv((function (acc, k, v) {
if (squint_core.truth_(squint_core.map_QMARK_(v))) {
return squint_core.assoc(acc, k, update_uuid_in_map(v, old_uuid, new_uuid))} else {
if (squint_core.truth_(squint_core.vector_QMARK_(v))) {
return squint_core.assoc(acc, k, update_uuid_in_coll(v, old_uuid, new_uuid))} else {
if (squint_core.truth_((() => {
const and__23227__auto__1 = squint_core.vector_QMARK_(v);
if (squint_core.truth_(and__23227__auto__1)) {
return (squint_core.second(v)) === (old_uuid)} else {
return and__23227__auto__1};

})())) {
return [k, new_uuid]} else {
if ((v) === (old_uuid)) {
return [k, new_uuid]} else {
if ("else") {
return squint_core.assoc(acc, k, v)} else {
return null}}}}};

}), ({  }), m);

};
var swap_uuids_BANG_ = function (p__13, old_uuid, new_id) {
const map__12 = p__13;
const ctx3 = map__12;
const store4 = squint_core.get(map__12, "store");
const setStore5 = squint_core.get(map__12, "setStore");
return setStore5((function (state) {
return update_uuid_in_map(state, old_uuid, new_id);

}));

};
var unwrap_proxy = function (data) {
if (squint_core.truth_(squint_core.array_QMARK_(data))) {
return squint_core.vec(squint_core.map(unwrap_proxy, data))} else {
if (squint_core.truth_(squint_core.vector_QMARK_(data))) {
return squint_core.mapv(unwrap_proxy, data)} else {
if (squint_core.truth_(squint_core.map_QMARK_(data))) {
return squint_core.into(({  }), squint_core.map((function (p__14) {
const vec__14 = p__14;
const k5 = squint_core.nth(vec__14, 0, null);
const v6 = squint_core.nth(vec__14, 1, null);
return [k5, unwrap_proxy(v6)];

}), data))} else {
if (squint_core.truth_(squint_core.object_QMARK_(data))) {
return squint_core.into(({  }), squint_core.map((function (p__15) {
const vec__710 = p__15;
const k11 = squint_core.nth(vec__710, 0, null);
const v12 = squint_core.nth(vec__710, 1, null);
return [k11, unwrap_proxy(v12)];

}), data))} else {
if ("else") {
return data} else {
return null}}}}};

};

export { swap_uuids_BANG_, ident_QMARK_, update_uuid_in_coll, unwrap_proxy, pull, traverse_and_transform, acc, add, update_uuid_in_map }
