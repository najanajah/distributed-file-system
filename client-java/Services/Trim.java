package Services;

import Helpers.Constants;
import Helpers.Runner;

public class Trim extends Service {

    public Trim(Runner r) {
        super(r);
        service_id = Constants.TRIM_ID;
    }

}
