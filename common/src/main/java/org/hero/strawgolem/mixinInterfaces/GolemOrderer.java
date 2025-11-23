package org.hero.strawgolem.mixinInterfaces;

import org.hero.strawgolem.golem.StrawGolem;

public interface GolemOrderer {
    StrawGolem strawgolemRewrite$getGolem();
    void strawgolemRewrite$setGolem(StrawGolem golem);
}
