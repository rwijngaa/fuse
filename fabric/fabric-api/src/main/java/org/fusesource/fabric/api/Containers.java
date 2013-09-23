/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.fabric.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper class for working with containers
 */
public class Containers {

    public static List<Container> containersForProfile(Container[] containers, String profileId) {
        List<Container> answer = new ArrayList<Container>();
        if (profileId != null) {
            for (Container c : containers) {
                for (Profile p : c.getProfiles()) {
                    if (profileId.equals(p.getId())) {
                        answer.add(c);
                    }
                }
            }
        }
        return answer;
    }

    /**
     * Creates a name for a new container given the current list of containers and the profile name.
     * For a profile of "foo" then this method tries to create a name of the form "foo1" or "foo2"
     * based on how many containers there are and if the name already exists.
     */
    public static String createContainerName(Container[] containers, String profile, String scheme, NameValidator nameValidator) {
        Map<String, Container> map = new HashMap<String, Container>();
        for (Container container : containers) {
            map.put(container.getId(), container);
        }
        String postFix = "." + scheme;
        String namePrefix = profile;
        if (namePrefix.endsWith(postFix)) {
            namePrefix = namePrefix.substring(0, namePrefix.length() - postFix.length());
        }
        // lets filter out non-alpha
        namePrefix = filterOutNonAlphaNumerics(namePrefix);
        List<Container> profileContainers = containersForProfile(containers, profile);
        int idx = profileContainers.size();
        while (true) {
            String name = namePrefix + Integer.toString(++idx);
            if (!map.containsKey(name) && nameValidator.isValid(name)) {
                return name;
            }
        }
    }

    private static String filterOutNonAlphaNumerics(String text) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, size = text.length(); i < size; i++) {
            char ch = text.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                builder.append(ch);
            }
        }
        return builder.toString();
    }
}
