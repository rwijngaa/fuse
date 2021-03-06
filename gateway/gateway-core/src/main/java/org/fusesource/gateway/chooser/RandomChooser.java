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
package org.fusesource.gateway.chooser;

import java.util.List;

/**
 * Randomly chooses a service in the list
 */
public class RandomChooser<T> implements Chooser<T> {

    @Override
    public T choose(List<T> things) {
        int size = things.size();
        if (size == 1) {
            return things.get(0);
        } else if (size > 1) {
            int idx = (int) Math.round(Math.random() * size);
            if (idx >= 0 && idx < size) {
                return things.get(idx);
            }
        }
        return null;
    }
}
