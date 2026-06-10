package com.workorder.repository;

import com.workorder.model.AssignRule;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AssignRuleRepository {

    private final Map<String, AssignRule> rules = new ConcurrentHashMap<>();

    public AssignRule save(AssignRule rule) {
        rules.put(rule.getId(), rule);
        return rule;
    }

    public Optional<AssignRule> findById(String id) {
        return Optional.ofNullable(rules.get(id));
    }

    public List<AssignRule> findAll() {
        return new ArrayList<>(rules.values());
    }

    public Optional<AssignRule> findEnabled() {
        return rules.values().stream()
                .filter(AssignRule::isEnabled)
                .findFirst();
    }

    public void deleteById(String id) {
        rules.remove(id);
    }
}
