package Services;

import Helpers.Constants;
import Helpers.Connection;

public class Create extends Service{

    public Create(Connection r) {
        super(r);
        service_id = Constants.CREATE_FILE_ID;
    }

}
