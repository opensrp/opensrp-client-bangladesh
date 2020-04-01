package org.smartregister.cbhc.domain;

import java.util.List;
import java.util.Set;

/**
 * Created by keyman on 3/1/2018.
 */

public class FormLocation {
    public String id;
    public String name;
    public String key;
    public Set<String> level;
    public List<FormLocation> nodes;
}
