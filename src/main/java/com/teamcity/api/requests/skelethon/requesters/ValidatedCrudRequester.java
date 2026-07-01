package com.teamcity.api.requests.skelethon.requesters;

import com.teamcity.api.requests.skelethon.interfaces.GetAllEndpointInterface;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import com.teamcity.models.BaseModel;
import com.teamcity.api.requests.skelethon.Endpoint;
import com.teamcity.api.requests.skelethon.HttpRequest;
import com.teamcity.api.requests.skelethon.interfaces.CrudEndpointInterface;

import java.util.Arrays;
import java.util.List;

public class ValidatedCrudRequester<M extends BaseModel> extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {
    private CrudRequester crudRequester;

    public ValidatedCrudRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification, Endpoint endpoint) {
        super(requestSpecification, responseSpecification, endpoint);
        this.crudRequester = new CrudRequester(requestSpecification, responseSpecification, endpoint);
    }

    @Override
    public M create(BaseModel model) {
        return (M) crudRequester.create(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public M create() {
        return (M) crudRequester.create().extract().as(endpoint.getResponseModel());
    }

    @Override
    public M read() {
        return (M) crudRequester.read().extract().as(endpoint.getResponseModel());
    }

    @Override
    public M read(long id) {
        return (M) crudRequester.read(id).extract().as(endpoint.getResponseModel());
    }

    @Override
    public M update(BaseModel model) {
        return (M) crudRequester.update(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public M delete(long id) {
        return (M) crudRequester.delete(id).extract().as(endpoint.getResponseModel());
    }

    @Override
    public List<M> getAll(Class<?> clazz) {
        M[] array = (M[]) crudRequester.getAll(clazz).extract().as(clazz);
        return Arrays.asList(array);
    }
}
