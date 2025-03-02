import * as squint_core from 'squint-cljs/core.js';
import * as string from 'squint-cljs/src/squint/string.js';
import * as l from 'lodash';
import { consola } from 'consola/browser';
var object_QMARK_ = function (o) {
return (typeof o) === ("object");
};
var get_ident = function (data) {
const temp__23739__auto__1 = squint_core.first(squint_core.filter((function (_PERCENT_1) {
return squint_core.re_find(/\/id$/, _PERCENT_1);
}), squint_core.keys(data)));
if (squint_core.truth_(temp__23739__auto__1)) {
const ident_key2 = temp__23739__auto__1;
return [ident_key2, squint_core.get(data, ident_key2)];}
};
var get_ns = function (k) {
return l.trim(squint_core.first(string.split(squint_core.first(k), "/")));
};
var remove_ident = function (ident, v) {
return squint_core.filterv((function (y) {
return !((squint_core.second(y)) === (squint_core.second(ident)));
}), v);
};
var ident_QMARK_ = function (x) {
const and__24226__auto__1 = squint_core.vector_QMARK_(x);
if (squint_core.truth_(and__24226__auto__1)) {
const and__24226__auto__2 = squint_core.string_QMARK_(squint_core.first(x));
if (squint_core.truth_(and__24226__auto__2)) {
const and__24226__auto__3 = (2) === (squint_core.count(x));
if (and__24226__auto__3) {
const or__24193__auto__4 = squint_core.string_QMARK_(squint_core.second(x));
if (squint_core.truth_(or__24193__auto__4)) {
return or__24193__auto__4;} else {
const or__24193__auto__5 = squint_core.number_QMARK_(squint_core.second(x));
if (squint_core.truth_(or__24193__auto__5)) {
return or__24193__auto__5;} else {
return (void 0 === squint_core.second(x));}}} else {
return and__24226__auto__3;}} else {
return and__24226__auto__2;}} else {
return and__24226__auto__1;}
};
var string_QMARK_ = function (thing) {
return (typeof thing) === ("string");
};
var uuid_QMARK_ = function (s) {
return squint_core.re_matches(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/, s);
};
var stream_id_QMARK_ = function (s) {
return squint_core.re_matches(/^kjz[a-zA-Z0-9]{43,}$/, s);
};
var template = function (s) {
return l.template(s);
};
var remove_item = function (v, item) {
return squint_core.vec(squint_core.filter((function (_PERCENT_1) {
return (_PERCENT_1) !== (item);
}), v));
};
var uuid = function () {
return crypto.randomUUID();
};
var camel_case = l.camelCase;
var kebab_case = l.kebabCase;
var pascal_case = function (s) {
return l.startCase(l.camelCase(s));
};
var e__GT_v = function (e) {
return squint_core.get(squint_core.get(e, "target"), "value");
};
var remove_ns = function (thing) {
if (squint_core.truth_(squint_core.vector_QMARK_(thing))) {
return squint_core.mapv(remove_ns, thing);} else {
if (squint_core.truth_(string_QMARK_(thing))) {
const or__24193__auto__1 = squint_core.second(string.split(thing, "/"));
if (squint_core.truth_(or__24193__auto__1)) {
return or__24193__auto__1;} else {
return thing;}} else {
if (squint_core.truth_(squint_core.map_QMARK_(thing))) {
return squint_core.zipmap(squint_core.mapv(remove_ns, squint_core.keys(thing)), remove_ns(squint_core.vals(thing)));} else {
return null;}}}
};
var copy_to_clipboard = function (text_to_copy) {
return navigator.clipboard.writeText(text_to_copy);
};
var random_evm = function () {
return string.join("0x", crypto.randomBytes(32).toString("hex"));
};
var drop_false = function (m) {
return squint_core.into(({  }), squint_core.filterv((function (x) {
const and__24226__auto__1 = squint_core.not(squint_core.false_QMARK_(squint_core.second(x)));
if (and__24226__auto__1) {
return !((squint_core.second(x) == null));} else {
return and__24226__auto__1;}
}), m));
};
var trunc_id = function (s) {
return s.substring((s["length"]) - (8));
};
var distribute = function (f, m) {
if (squint_core.truth_(squint_core.vector_QMARK_(m))) {
return f(squint_core.mapv((function (_PERCENT_1) {
return distribute(f, _PERCENT_1);
}), m));} else {
if (squint_core.truth_((() => {
const or__24193__auto__1 = squint_core.map_QMARK_(m);
if (squint_core.truth_(or__24193__auto__1)) {
return or__24193__auto__1;} else {
return object_QMARK_(m);}
})())) {
return f(squint_core.zipmap(squint_core.keys(m), squint_core.mapv((function (_PERCENT_1) {
return distribute(f, _PERCENT_1);
}), squint_core.vals(m))));} else {
if ("else") {
return m;} else {
return null;}}}
};
var nsd = function (data, ns) {
return squint_core.zipmap(squint_core.mapv((function (x) {
return squint_core.str(ns, "/", x);
}), squint_core.keys(data)), squint_core.vals(data));
};
var add_ns = function (data) {
return distribute((function (e) {
if (squint_core.truth_(squint_core.contains_QMARK_(e, "edges"))) {
return add_ns(squint_core.vals(squint_core.get(e, "edges")));} else {
if (squint_core.truth_(squint_core.contains_QMARK_(e, "node"))) {
return add_ns(squint_core.get(e, "node"));} else {
if (squint_core.truth_(squint_core.contains_QMARK_(e, "__typename"))) {
const n1 = kebab_case(squint_core.get(e, "__typename"));
return nsd(squint_core.dissoc(e, "__typename"), n1);} else {
if ("else") {
return e;} else {
return null;}}}}
}), data);
};
var set_item_BANG_ = function (key, val) {
return window.localStorage.setItem(key, JSON.stringify(val));
};
var get_item = function (key) {
return (() => {
try{
return JSON.parse(window.localStorage.getItem(key));}
catch(e1){
consola.error(squint_core.str("could net get item: ", key, " "), e1);
return null;}

})();
};
var remove_item_BANG_ = function (key) {
return window.localStorage.removeItem(key);
};
var set_session_item_BANG_ = function (key, val) {
return window.sessionStorage.setItem(key, JSON.stringify(val));
};
var get_session_item = function (key) {
return (() => {
try{
return JSON.parse(window.sessionStorage.getItem(key));}
catch(e1){
consola.error(squint_core.str("could net get item: ", key, " "), e1);
return null;}

})();
};
var remove_session_item_BANG_ = function (key) {
return window.sessionStorage.removeItem(key);
};
var distinct_second_elements = function (coll) {
return squint_core.vec((() => {
const seen1 = squint_core.atom(new Set([]));
return squint_core.filter((function (p__27) {
const vec__25 = p__27;
const _6 = squint_core.nth(vec__25, 0, null);
const second7 = squint_core.nth(vec__25, 1, null);
if (squint_core.truth_(squint_core.contains_QMARK_(squint_core.deref(seen1), second7))) {
return false;} else {
squint_core.swap_BANG_(seen1, squint_core.conj, second7);
return true;}
}), coll);
})());
};
var is_uuid_QMARK_ = function (val) {
return squint_core.re_matches(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/, val);
};

export { e__GT_v, string_QMARK_, is_uuid_QMARK_, remove_ident, kebab_case, stream_id_QMARK_, remove_item_BANG_, distinct_second_elements, add_ns, get_item, ident_QMARK_, set_item_BANG_, random_evm, drop_false, pascal_case, copy_to_clipboard, uuid_QMARK_, remove_session_item_BANG_, uuid, distribute, object_QMARK_, nsd, remove_item, template, get_ident, remove_ns, trunc_id, get_session_item, camel_case, get_ns, set_session_item_BANG_ }
