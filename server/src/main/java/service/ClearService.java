package service;

import dataaccess.DataAccess;
import exception.DataAccessException;
import requests.ClearRequest;
import results.ClearResult;

public class ClearService {
    private final DataAccess dataAccess;

    public ClearService(DataAccess dataAccess){ this.dataAccess = dataAccess; }

    public ClearResult clear(ClearRequest request) throws DataAccessException {
        dataAccess.clear( request );
        return new ClearResult();
    }
}
