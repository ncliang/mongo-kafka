/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Original Work: Apache License, Version 2.0, Copyright 2017 Hans-Peter Grahsl.
 */

package at.grahsl.kafka.connect.mongodb.processor.field.renaming;

import java.util.Objects;

// TODO - used ? Tested?
public class FieldnameMapping {

    private String oldName;
    private String newName;

    public FieldnameMapping() {
    }

    public FieldnameMapping(final String oldName, final String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    public String toString() {
        return "FieldnameMapping{"
                + "oldName='" + oldName + '\''
                + ", newName='" + newName + '\''
                + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldnameMapping that = (FieldnameMapping) o;

        if (!Objects.equals(oldName, that.oldName)) {
            return false;
        }
        return newName != null ? newName.equals(that.newName) : that.newName == null;
    }

    @Override
    public int hashCode() {
        int result = oldName != null ? oldName.hashCode() : 0;
        result = 31 * result + (newName != null ? newName.hashCode() : 0);
        return result;
    }
}
