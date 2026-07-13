package com.teamcity.api.requests.skelethon.requesters;

import com.teamcity.api.requests.skelethon.interfaces.GetAllEndpointInterface;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import com.teamcity.api.models.BaseModel;
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
    public M post(BaseModel model) {
        return (M) crudRequester.post(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public M post() {
        return (M) crudRequester.post().extract().as(endpoint.getResponseModel());
    }

    @Override
    public M get() {
        return (M) crudRequester.get().extract().as(endpoint.getResponseModel());
    }

    @Override
    public M get(long id) {
        return (M) crudRequester.get(id).extract().as(endpoint.getResponseModel());
    }

    @Override
    public M get(String id) {
        return (M) crudRequester.get(id).extract().as(endpoint.getResponseModel());
    }

    @Override
    public M put(BaseModel model) {
        return (M) crudRequester.put(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public M put(String id, BaseModel model) {
        return (M) crudRequester.put(id, model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public M delete(long id) {
        return (M) crudRequester.delete(id).extract().as(endpoint.getResponseModel());
    }

    @Override
    public M delete(String id) {
        return (M) crudRequester.delete(id).extract().as(endpoint.getResponseModel());
    }

    @Override
    public List<M> getAll(Class<?> clazz) {
        M[] array = (M[]) crudRequester.getAll(clazz).extract().as(clazz);
        return Arrays.asList(array);
    }
}
