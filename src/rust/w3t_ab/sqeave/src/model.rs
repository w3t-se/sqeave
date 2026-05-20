use std::collections::BTreeMap;

pub type Table = String;
pub type Id = String;
pub type AttrKey = String;

#[derive(Clone, Debug, PartialEq, Eq, PartialOrd, Ord)]
pub struct Ident {
    pub table: Table,
    pub id: Id,
}

impl Ident {
    pub fn new(table: impl Into<String>, id: impl Into<String>) -> Self {
        Self {
            table: table.into(),
            id: id.into(),
        }
    }
}

#[derive(Clone, Debug, PartialEq)]
pub enum AttrValue {
    Null,
    Bool(bool),
    Number(f64),
    String(String),
    Ident(Ident),
    List(Vec<AttrValue>),
    Map(BTreeMap<AttrKey, AttrValue>),
}

#[derive(Clone, Debug, PartialEq)]
pub struct Entity {
    pub ident: Ident,
    pub attrs: BTreeMap<AttrKey, AttrValue>,
}

pub type Db = BTreeMap<Table, BTreeMap<Id, Entity>>;

pub fn is_ident_key(k: &str) -> bool {
    k.ends_with("/id")
}
