package io.github.pizzaserver.api.entity.definition;

import io.github.pizzaserver.api.entity.Entity;
import io.github.pizzaserver.api.entity.definition.components.EntityComponentEvent;
import io.github.pizzaserver.api.entity.definition.components.EntityComponentGroup;
import io.github.pizzaserver.api.entity.definition.spawnrules.EntitySpawnRules;

public interface EntityDefinition {

    String getId();

    String getName();

    boolean isSummonable();

    boolean hasSpawnEgg();

    EntityComponentGroup getComponentGroup(String groupId);

    EntityComponentEvent getEvent(String eventId);

    EntitySpawnRules getSpawnRules();

    Entity create();

    void onCreation(Entity entity);

}
