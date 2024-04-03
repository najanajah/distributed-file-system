package Services;

import Helpers.Constants;
import Helpers.Connection;

public class Clear extends Service {

    public Clear(Connection r) {
        super(r);
        service_id = Constants.CLEAR_ID;
    }

}
