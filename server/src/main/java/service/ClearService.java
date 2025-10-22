package service;

import dataaccess.DataAccess;
import model.ClearRequest;
import model.ClearResult;

public class ClearService {
    private final DataAccess dataAccess;

    public ClearService(DataAccess dataAccess){ this.dataAccess = dataAccess; }

    public ClearResult clear(ClearRequest request){
        dataAccess.clear( request );
        return new ClearResult();
    }
}
