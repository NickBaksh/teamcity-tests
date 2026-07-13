package com.teamcity.api.requests.skelethon.interfaces;

import com.teamcity.api.models.BaseModel;

public interface CrudEndpointInterface {
    Object post();
    Object post(BaseModel model); //POST запрос
    Object get(); //GET запрос
    Object get(long id); //GET запрос где в URL передаем id
    Object get(String id);
    Object put(BaseModel model); //PUT запрос
    Object put(String id, BaseModel model);
    Object delete(long id);
    Object delete(String id);
}