use crate::model::{AttrValue, Entity, Ident};
use std::collections::{BTreeMap, BTreeSet};

#[derive(Default)]
pub struct GraphIndex {
    pub deps: BTreeMap<Ident, BTreeSet<Ident>>,
    pub rdeps: BTreeMap<Ident, BTreeSet<Ident>>,
    pub dirty: BTreeSet<Ident>,
    pub running: BTreeSet<Ident>,
    pub versions: BTreeMap<Ident, u64>,
    pub executed_versions: BTreeMap<Ident, u64>,
}

impl GraphIndex {
    pub fn reindex_entity(&mut self, entity: &Entity) {
        let ident = &entity.ident;

        if let Some(old_deps) = self.deps.remove(ident) {
            for dep in old_deps {
                if let Some(children) = self.rdeps.get_mut(&dep) {
                    children.remove(ident);
                }
            }
        }

        let mut deps = BTreeSet::new();

        for v in entity.attrs.values() {
            collect_idents(v, &mut deps);
        }

        deps.remove(ident);

        for dep in &deps {
            self.rdeps
                .entry(dep.clone())
                .or_default()
                .insert(ident.clone());
        }

        self.deps.insert(ident.clone(), deps);
    }

    pub fn bump_version(&mut self, ident: &Ident) {
        let next = self.versions.get(ident).copied().unwrap_or(0) + 1;
        self.versions.insert(ident.clone(), next);
    }

    pub fn mark_dirty_downstream(&mut self, ident: &Ident) {
        let mut stack = vec![ident.clone()];

        while let Some(current) = stack.pop() {
            if self.dirty.insert(current.clone()) {
                if let Some(children) = self.rdeps.get(&current) {
                    for child in children {
                        stack.push(child.clone());
                    }
                }
            }
        }
    }

    pub fn ready(&self, ident: &Ident) -> bool {
        let deps = self.deps.get(ident).cloned().unwrap_or_default();

        deps.iter().all(|dep| {
            let dep_v = self.versions.get(dep).copied().unwrap_or(0);
            let done_v = self.executed_versions.get(dep).copied().unwrap_or(0);

            dep_v > 0 && dep_v == done_v
        })
    }

    pub fn next_runnable(&self) -> Vec<Ident> {
        self.dirty
            .iter()
            .filter(|id| !self.running.contains(*id))
            .filter(|id| self.ready(id))
            .cloned()
            .collect()
    }

    pub fn mark_running(&mut self, ident: &Ident) {
        self.running.insert(ident.clone());
    }

    pub fn mark_done(&mut self, ident: &Ident) {
        let v = self.versions.get(ident).copied().unwrap_or(0);
        self.executed_versions.insert(ident.clone(), v);
        self.running.remove(ident);
        self.dirty.remove(ident);
    }
}

pub fn collect_idents(v: &AttrValue, out: &mut BTreeSet<Ident>) {
    match v {
        AttrValue::Ident(i) => {
            out.insert(i.clone());
        }

        AttrValue::List(items) => {
            for x in items {
                collect_idents(x, out);
            }
        }

        AttrValue::Map(map) => {
            for x in map.values() {
                collect_idents(x, out);
            }
        }

        _ => {}
    }
}
