use crate::model::{is_ident_key, AttrValue, Entity, Ident};
use serde_json::{json, Map, Value};
use std::collections::BTreeMap;

pub fn value_to_attr(v: Value) -> AttrValue {
    match v {
        Value::Null => AttrValue::Null,
        Value::Bool(b) => AttrValue::Bool(b),
        Value::Number(n) => AttrValue::Number(n.as_f64().unwrap_or(0.0)),
        Value::String(s) => AttrValue::String(s),

        Value::Array(items) => {
            if items.len() == 2 {
                if let Some(table) = items[0].as_str() {
                    if is_ident_key(table) {
                        let id = match &items[1] {
                            Value::String(s) => Some(s.clone()),
                            Value::Number(n) => Some(n.to_string()),
                            _ => None,
                        };

                        if let Some(id) = id {
                            return AttrValue::Ident(Ident::new(table, id));
                        }
                    }
                }
            }

            AttrValue::List(items.into_iter().map(value_to_attr).collect())
        }

        Value::Object(obj) => {
            AttrValue::Map(
                obj.into_iter()
                    .map(|(k, v)| (k, value_to_attr(v)))
                    .collect(),
            )
        }
    }
}

pub fn attr_to_value(v: AttrValue) -> Value {
    match v {
        AttrValue::Null => Value::Null,
        AttrValue::Bool(b) => Value::Bool(b),
        AttrValue::Number(n) => json!(n),
        AttrValue::String(s) => Value::String(s),

        AttrValue::Ident(ident) => json!([ident.table, ident.id]),

        AttrValue::List(items) => {
            Value::Array(items.into_iter().map(attr_to_value).collect())
        }

        AttrValue::Map(map) => {
            Value::Object(
                map.into_iter()
                    .map(|(k, v)| (k, attr_to_value(v)))
                    .collect::<Map<_, _>>(),
            )
        }
    }
}

pub fn entity_to_value(entity: Entity, strip_content: bool) -> Value {
    let mut obj = Map::new();

    obj.insert(
        entity.ident.table.clone(),
        Value::String(entity.ident.id.clone()),
    );

    for (k, v) in entity.attrs {
        if strip_content && (k == "data/content" || k == ":data/content") {
            continue;
        }

        obj.insert(k, attr_to_value(v));
    }

    Value::Object(obj)
}

pub fn value_get_ident(v: &Value) -> Option<Ident> {
    let obj = v.as_object()?;

    for (k, val) in obj {
        if is_ident_key(k) {
            let id = match val {
                Value::String(s) => s.clone(),
                Value::Number(n) => n.to_string(),
                _ => continue,
            };

            return Some(Ident::new(k, id));
        }
    }

    None
}

pub fn value_to_entity(v: Value) -> Option<Entity> {
    let ident = value_get_ident(&v)?;
    let obj = v.as_object()?.clone();

    let mut attrs = BTreeMap::new();

    for (k, val) in obj {
        attrs.insert(k, value_to_attr(val));
    }

    attrs.insert(
        ident.table.clone(),
        AttrValue::String(ident.id.clone()),
    );

    Some(Entity { ident, attrs })
}
