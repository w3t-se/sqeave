import * as squint_core from 'squint-cljs/core.js';
import * as u from './utils.mjs';
import { consola } from 'consola/browser';
import { reconcile } from 'solid-js/store';
var ident_QMARK_ = function (data) {
if (squint_core.truth_(squint_core.vector_QMARK_(data))) {
if (squint_core.truth_(squint_core.string_QMARK_(squint_core.first(data)))) {
const and__24226__auto__1 = squint_core.re_find(/\/id$/, squint_core.first(data));
if (squint_core.truth_(and__24226__auto__1)) {
const and__24226__auto__2 = (squint_core.count(data)) === (2);
if (and__24226__auto__2) {
const or__24193__auto__3 = squint_core.number_QMARK_(squint_core.second(data));
if (squint_core.truth_(or__24193__auto__3)) {
return or__24193__auto__3;} else {
const or__24193__auto__4 = squint_core.string_QMARK_(squint_core.second(data));
if (squint_core.truth_(or__24193__auto__4)) {
return or__24193__auto__4;} else {
return (void 0 === squint_core.second(data));}}} else {
return and__24226__auto__2;}} else {
return and__24226__auto__1;}} else {
return false;}} else {
return false;}
};
var traverse_and_transform = function (item, setStore) {
if (squint_core.truth_(squint_core.vector_QMARK_(item))) {
return squint_core.mapv((function (_PERCENT_1) {
return traverse_and_transform(_PERCENT_1, setStore);
}), item);} else {
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
return ident1;} else {
return new_val2;}} else {
if ("else") {
return item;} else {
return null;}}}
};
var acc = squint_core.atom(({  }));
var add = (() => {
const f6 = (function (var_args) {
const args71 = [];
const len__24850__auto__2 = arguments.length;
let i83 = 0;
while(true){
if ((i83) < (len__24850__auto__2)) {
args71.push((arguments[i83]));
let G__4 = (i83 + 1);
i83 = G__4;
continue;
};break;
}
;
const argseq__25251__auto__5 = (((1) < (args71.length)) ? (args71.slice(1)) : (null));
return f6.cljs$core$IFn$_invoke$arity$variadic((arguments[0]), argseq__25251__auto__5);
});
f6.cljs$core$IFn$_invoke$arity$variadic = (function (p__11, data) {
const map__67 = p__11;
const ctx8 = map__67;
const store9 = squint_core.get(map__67, "store");
const setStore10 = squint_core.get(map__67, "setStore");
const res11 = traverse_and_transform((() => {
const or__24193__auto__12 = squint_core.first(data);
if (squint_core.truth_(or__24193__auto__12)) {
return or__24193__auto__12;} else {
return store9;}
})(), acc);
consola.debug("rr: ", res11);
consola.debug("rr:acc ", squint_core.deref(acc));
if (squint_core.not(squint_core.first(data))) {
consola.debug("merge-data: ", squint_core.merge_with(squint_core.merge, res11, squint_core.deref(acc)));
setStore10(reconcile(squint_core.merge_with(squint_core.merge, res11, squint_core.deref(acc))))} else {
squint_core.reduce_kv((function (m, k, v) {
consola.debug("set: ", k, " ", v);
consola.debug("set2: ", squint_core.merge_with(squint_core.merge, squint_core.get(store9, k), v));
return setStore10(k, (function (_PERCENT_1) {
return squint_core.merge_with(squint_core.merge, _PERCENT_1, v);
}));
}), ({  }), ((squint_core.not(squint_core.vector_QMARK_(res11))) ? (squint_core.merge(squint_core.deref(acc), res11)) : (squint_core.deref(acc))))};
squint_core.reset_BANG_(acc, ({  }));
return res11;
});
f6.cljs$lang$maxFixedArity = 1;
f6.cljs$lang$applyTo = (function (seq9) {
const G__1013 = squint_core.first(seq9);
const seq914 = squint_core.next(seq9);
const self__24952__auto__15 = this;
return self__24952__auto__15.cljs$core$IFn$_invoke$arity$variadic(G__1013, seq914);
});
return f6;
})();
var pull = function (store, entity, query) {
if (squint_core.truth_(ident_QMARK_(entity))) {
return pull(store, squint_core.get_in(store, entity), query);} else {
if (squint_core.truth_((() => {
const and__24226__auto__1 = (squint_core.count(entity)) > (0);
if (and__24226__auto__1) {
return squint_core.vector_QMARK_(entity);} else {
return and__24226__auto__1;}
})())) {
return squint_core.mapv((function (x) {
if (squint_core.truth_(ident_QMARK_(x))) {
return pull(store, x, query);} else {
return x;}
}), entity);} else {
if (squint_core.truth_((() => {
const and__24226__auto__2 = (squint_core.count(query)) > (1);
if (and__24226__auto__2) {
return squint_core.vector_QMARK_(query);} else {
return and__24226__auto__2;}
})())) {
const simple_keys3 = squint_core.filterv(squint_core.string_QMARK_, query);
const not_simple4 = squint_core.filterv((function (_PERCENT_1) {
return squint_core.not(squint_core.string_QMARK_(_PERCENT_1));
}), query);
return squint_core.into(squint_core.zipmap(simple_keys3, squint_core.mapv((function (_PERCENT_1) {
return pull(store, entity, _PERCENT_1);
}), simple_keys3)), squint_core.mapv((function (_PERCENT_1) {
return pull(store, entity, _PERCENT_1);
}), not_simple4));} else {
if (squint_core.truth_((() => {
const and__24226__auto__5 = (squint_core.count(query)) === (1);
if (and__24226__auto__5) {
return squint_core.vector_QMARK_(query);} else {
return and__24226__auto__5;}
})())) {
return pull(store, entity, squint_core.first(query));} else {
if (squint_core.truth_(squint_core.map_QMARK_(query))) {
const nk6 = squint_core.first(squint_core.keys(query));
const sub_query7 = squint_core.get(query, nk6);
const temp__23739__auto__8 = squint_core.get(entity, nk6);
if (squint_core.truth_(temp__23739__auto__8)) {
const data9 = temp__23739__auto__8;
const G__1210 = ({  });
(G__1210[nk6] = ((squint_core.truth_(ident_QMARK_(data9))) ? (pull(store, data9, sub_query7)) : (squint_core.mapv((function (_PERCENT_1) {
return pull(store, _PERCENT_1, sub_query7);
}), data9))));
return G__1210;}} else {
if ("else") {
return squint_core.get(entity, query);} else {
return null;}}}}}}
};
var update_uuid_in_coll = function (coll, old_uuid, new_uuid) {
return squint_core.mapv((function (item) {
if (squint_core.truth_(squint_core.map_QMARK_(item))) {
return update_uuid_in_map(item, old_uuid, new_uuid);} else {
if (squint_core.truth_(squint_core.vector_QMARK_(item))) {
if ((squint_core.second(item)) === (old_uuid)) {
return squint_core.assoc(item, 1, new_uuid);} else {
return update_uuid_in_coll(item, old_uuid, new_uuid);}} else {
if ("else") {
return item;} else {
return null;}}}
}), coll);
};
var update_uuid_in_map = function (m, old_uuid, new_uuid) {
return squint_core.reduce_kv((function (acc, k, v) {
if (squint_core.truth_(squint_core.map_QMARK_(v))) {
return squint_core.assoc(acc, k, update_uuid_in_map(v, old_uuid, new_uuid));} else {
if (squint_core.truth_(squint_core.vector_QMARK_(v))) {
return squint_core.assoc(acc, k, update_uuid_in_coll(v, old_uuid, new_uuid));} else {
if (squint_core.truth_((() => {
const and__24226__auto__1 = squint_core.vector_QMARK_(v);
if (squint_core.truth_(and__24226__auto__1)) {
return (squint_core.second(v)) === (old_uuid);} else {
return and__24226__auto__1;}
})())) {
return [k, new_uuid];} else {
if ((v) === (old_uuid)) {
return [k, new_uuid];} else {
if ("else") {
return squint_core.assoc(acc, k, v);} else {
return null;}}}}}
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
return squint_core.vec(squint_core.map(unwrap_proxy, data));} else {
if (squint_core.truth_(squint_core.vector_QMARK_(data))) {
return squint_core.mapv(unwrap_proxy, data);} else {
if (squint_core.truth_(squint_core.map_QMARK_(data))) {
return squint_core.into(({  }), squint_core.map((function (p__14) {
const vec__14 = p__14;
const k5 = squint_core.nth(vec__14, 0, null);
const v6 = squint_core.nth(vec__14, 1, null);
return [k5, unwrap_proxy(v6)];
}), data));} else {
if (squint_core.truth_(squint_core.object_QMARK_(data))) {
return squint_core.into(({  }), squint_core.map((function (p__15) {
const vec__710 = p__15;
const k11 = squint_core.nth(vec__710, 0, null);
const v12 = squint_core.nth(vec__710, 1, null);
return [k11, unwrap_proxy(v12)];
}), data));} else {
if ("else") {
return data;} else {
return null;}}}}}
};

export { swap_uuids_BANG_, ident_QMARK_, update_uuid_in_coll, unwrap_proxy, pull, traverse_and_transform, acc, add, update_uuid_in_map }
