package service;

import dataaccess.DataAccess;
import Requests.ClearRequest;
import Results.ClearResult;

public class ClearService {
    private final DataAccess dataAccess;

    public ClearService(DataAccess dataAccess){ this.dataAccess = dataAccess; }

    public ClearResult clear(ClearRequest request){
        dataAccess.clear( request );
        return new ClearResult();
    }
}
