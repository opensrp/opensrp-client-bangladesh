package util;

import org.smartregister.service.ANMService;
import org.smartregister.util.Cache;
import org.smartregister.view.contract.HomeContext;
import org.smartregister.view.controller.ANMController;

/**
 * Created by kaderchowdhury on 09/12/17.
 */

public class ANMControllerMock extends ANMController {
    String locationString = "{\"locationsHierarchy\":{\"children\":{\"keys\":{\"map\":{\"node\":{\"name\":\"CMH\",\"locationId\":\"1\"}}}}}}";
    public ANMControllerMock(ANMService anmService, Cache<String> cache, Cache<HomeContext> homeContextCache) {
        super(anmService, cache, homeContextCache);
    }

    @Override
    public String get() {
        return locationString;
    }
}
