/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.launcher.daemon.server.health;

import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.launcher.daemon.server.health.gc.GarbageCollectionMonitor;

import static java.lang.String.format;

class DaemonStatus {

    public static final String EXPIRE_AT_PROPERTY = "org.gradle.daemon.performance.expire-at";
    static final int DEFAULT_EXPIRE_AT = 0;

    boolean isDaemonTired(DaemonStats stats) {
        String expireAt = System.getProperty(EXPIRE_AT_PROPERTY);
        int threshold = parseValue(expireAt, DEFAULT_EXPIRE_AT);
        return threshold != 0 //zero means the feature is off
                && (isOldGenExhausted(stats) || isPermGenExhausted(stats));
    }

    boolean isOldGenExhausted(DaemonStats stats) {
        return stats.getOldGenStats().getCount() == GarbageCollectionMonitor.EVENT_WINDOW // we have a full window of GC events
            && stats.getOldGenStats().getUsage() > 80 // we are consistently above 80% heap usage after GC
            && stats.getOldGenStats().getRate() > 1.5;// we are GC'ing faster than 1.5 times a second
    }

    boolean isPermGenExhausted(DaemonStats stats) {
        if (JavaVersion.current().isJava8Compatible()) {
            return false;
        } else {
            return stats.getOldGenStats().getCount() == GarbageCollectionMonitor.EVENT_WINDOW // we have a full window of GC events
                && stats.getPermGenStats().getUsage() > 85; // we are consistently above 85% of Perm Gen after GC
        }
    }

    private static int parseValue(String expireAt, int defaultValue) {
        if (expireAt == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(expireAt);
        } catch (Exception e) {
            throw new GradleException(format(
                    "System property '%s' has incorrect value: '%s'. The value needs to be integer.",
                    EXPIRE_AT_PROPERTY, expireAt));
        }
    }
}
