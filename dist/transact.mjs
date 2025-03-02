import * as squint_core from 'squint-cljs/core.js';
import * as n from './normad.mjs';
import * as u from './utils.mjs';
import * as log from 'loglevel';
var alert_error = function (ctx, error) {
return n.add(ctx, ({ "component/id": "alert", "title": "Error", "visible?": true, "type": "error", "interval": 4000, "message": squint_core.str(error) }), ({  }));
};
var get_viewer_user = function (p__16) {
const map__12 = p__16;
const ctx3 = map__12;
const store4 = squint_core.get(map__12, "store");
const setStore5 = squint_core.get(map__12, "setStore");
return n.pull(store4, ["viewer/id", 0], [({ "viewer/user": ["user/id", "user/session", "user/account"] })]);
};
var viewer_ident = function (ctx) {
return squint_core.get(squint_core.get(get_viewer_user(ctx), "viewer/user"), "user/account");
};
var viewer_QMARK_ = function (ctx, acc) {
const viewer_id1 = squint_core.second(viewer_ident(ctx));
const and__24235__auto__2 = (viewer_id1) === (acc);
if (and__24235__auto__2) {
const and__24235__auto__3 = !((viewer_id1 == null));
if (and__24235__auto__3) {
return !((acc == null));} else {
return and__24235__auto__3;}} else {
return and__24235__auto__2;}
};
var check_session = function (p__17) {
const map__12 = p__17;
const ctx3 = map__12;
const store4 = squint_core.get(map__12, "store");
const setStore5 = squint_core.get(map__12, "setStore");
if (squint_core.not(squint_core.get_in(get_viewer_user(ctx3), ["viewer/user", "user/session"]))) {
throw new Error("Sign in to to make changes.")}
};
var wrap_session = function (ctx, check_session_QMARK_, f) {
return (() => {
try{
if (squint_core.truth_(check_session_QMARK_)) {
check_session(ctx)};
return f();}
catch(e1){
alert_error(ctx, e1);
return log.error(e1);}

})();
};
var set_field_BANG_ = function (p__18, value, p__19) {
const map__13 = p__18;
const ctx4 = map__13;
const store5 = squint_core.get(map__13, "store");
const setStore6 = squint_core.get(map__13, "setStore");
const map__27 = p__19;
const param8 = map__27;
const append9 = squint_core.get(map__27, "append", false);
const replace10 = squint_core.get(map__27, "replace", false);
const check_session_QMARK_11 = squint_core.get(map__27, "check-session?", false);
return wrap_session(ctx4, check_session_QMARK_11, (function () {
const path12 = (() => {
const or__24212__auto__13 = append9;
if (squint_core.truth_(or__24212__auto__13)) {
return or__24212__auto__13;} else {
return replace10;}
})();
const action14 = (squint_core.truth_(append9)) ? ((squint_core.truth_(replace10)) ? ((function (_PERCENT_1) {
return u.distinct_second_elements(squint_core.conj(_PERCENT_1, value));
})) : ((function (_PERCENT_1) {
return squint_core.conj(_PERCENT_1, value);
}))) : ((function () {
return squint_core.identity(value);
}));
return squint_core.apply(setStore6, squint_core.conj(path12, (function (x) {
return action14(x);
})));
}));
};
var add_ident_BANG_ = function (p__20, ident, p__21) {
const map__13 = p__20;
const ctx4 = map__13;
const store5 = squint_core.get(map__13, "store");
const setStore6 = squint_core.get(map__13, "setStore");
const map__27 = p__21;
const param8 = map__27;
const append9 = squint_core.get(map__27, "append", false);
const replace10 = squint_core.get(map__27, "replace", false);
const check_session_QMARK_11 = squint_core.get(map__27, "check-session?", false);
return set_field_BANG_(ctx4, ident, param8);
};
var remove_ident_BANG_ = function (p__22, path, ident, p__23) {
const map__13 = p__22;
const ctx4 = map__13;
const store5 = squint_core.get(map__13, "store");
const setStore6 = squint_core.get(map__13, "setStore");
const map__27 = p__23;
const check_session_QMARK_8 = squint_core.get(map__27, "check-session?", false);
return wrap_session(ctx4, check_session_QMARK_8, (function () {
return squint_core.apply(setStore6, squint_core.conj(path, (function (x) {
return (() => {
try{
return u.remove_ident(ident, x);}
catch(e9){
log.error(e9);
return x;}

})();
})));
}));
};
var add_BANG_ = function (p__24, value, p__25) {
const map__13 = p__24;
const ctx4 = map__13;
const store5 = squint_core.get(map__13, "store");
const setStore6 = squint_core.get(map__13, "setStore");
const map__27 = p__25;
const params8 = map__27;
const append9 = squint_core.get(map__27, "append", false);
const replace10 = squint_core.get(map__27, "replace", false);
const after11 = squint_core.get(map__27, "after", false);
const check_session_QMARK_12 = squint_core.get(map__27, "check-session?", false);
return wrap_session(ctx4, check_session_QMARK_12, (function () {
const res13 = n.add(ctx4, value);
if (squint_core.truth_((() => {
const or__24212__auto__14 = append9;
if (squint_core.truth_(or__24212__auto__14)) {
return or__24212__auto__14;} else {
return replace10;}
})())) {
add_ident_BANG_(ctx4, res13, params8)};
if (squint_core.truth_(after11)) {
return after11();}
}));
};
var remove_entity_BANG_ = function () {
return null;
};
var swap_uuids_BANG_ = function (p__26, ident, stream_id) {
const map__12 = p__26;
const ctx3 = map__12;
const store4 = squint_core.get(map__12, "store");
const setStore5 = squint_core.get(map__12, "setStore");
const n16 = squint_core.first(ident);
const new_ident7 = [n16, stream_id];
const obj8 = squint_core.get_in(store4, ident);
const new_obj9 = squint_core.assoc(obj8, n16, stream_id);
const paths10 = squint_core.get(obj8, "uuid/paths");
setStore5(squint_core.first(ident), (function (x) {
return squint_core.assoc(x, stream_id, new_obj9);
}));
log.debug("path: ", paths10);
return squint_core.mapv((function (_PERCENT_1) {
return squint_core.apply(setStore5, squint_core.conj(_PERCENT_1, (function (x) {
if (squint_core.truth_(u.ident_QMARK_(x))) {
return new_ident7;} else {
if (squint_core.truth_(squint_core.vector_QMARK_(x))) {
return squint_core.conj(u.remove_ident(ident, x), new_ident7);}}
})));
}), paths10);
};

export { swap_uuids_BANG_, add_BANG_, get_viewer_user, add_ident_BANG_, remove_ident_BANG_, check_session, alert_error, wrap_session, remove_entity_BANG_, set_field_BANG_, viewer_ident, viewer_QMARK_ }
