package Services;

import Driver.Connection;
import Driver.Constants;

public class Create extends ServiceABC{

    public Create(Connection r) {
        super(r);
        service_id = Constants.CREATE_FILE_ID;
    }

}
