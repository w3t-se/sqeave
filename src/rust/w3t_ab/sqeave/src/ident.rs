use serde::{Deserialize, Serialize};
use serde_json::Value;

#[derive(Clone, Debug, PartialEq, Eq, PartialOrd, Ord, Serialize, Deserialize)]
pub struct Ident(pub String, pub String);

pub fn is_ident_key(k: &str) -> bool {
    k.ends_with("/id")
}

pub fn ident_from_array(v: &Value) -> Option<Ident> {
    let arr = v.as_array()?;
    if arr.len() != 2 {
        return None;
    }

    let table = arr[0].as_str()?;
    if !is_ident_key(table) {
        return None;
    }

    let id = match &arr[1] {
        Value::String(s) => s.clone(),
        Value::Number(n) => n.to_string(),
        Value::Null => return None,
        _ => return None,
    };

    Some(Ident(table.to_string(), id))
}

pub fn get_ident(v: &Value) -> Option<Ident> {
    let obj = v.as_object()?;

    for (k, val) in obj {
        if is_ident_key(k) {
            let id = match val {
                Value::String(s) => s.clone(),
                Value::Number(n) => n.to_string(),
                _ => continue,
            };

            return Some(Ident(k.clone(), id));
        }
    }

    None
}
