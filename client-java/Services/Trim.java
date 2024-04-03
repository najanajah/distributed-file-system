package Services;

import Helpers.Constants;
import Helpers.Connection;

public class Trim extends Service {

    public Trim(Connection r) {
        super(r);
        service_id = Constants.TRIM_ID;
    }

}
