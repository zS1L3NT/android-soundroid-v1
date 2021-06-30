package com.zectan.soundroid.models;

import com.zectan.soundroid.utils.Anonymous;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Info {
    private String id;
    private String name;
    private String cover;
    private String colorHex;
    private String userId;
    private List<String> order;
    private List<String> queries;

    public Info() {
    }

    public Info(String id, String name, List<String> order) {
        this.id = id;
        this.name = name;
        this.cover = "";
        this.colorHex = "#7b828b";
        this.userId = "";
        this.order = order;
        this.queries = Anonymous.getQueries(name);
    }

    public Info(String id, String name, String cover, String colorHex, String userId, List<String> order, List<String> queries) {
        this.id = id;
        this.name = name;
        this.cover = cover;
        this.colorHex = colorHex;
        this.userId = userId;
        this.order = order;
        this.queries = queries;
    }

    public static Info getEmpty() {
        return new Info("", "", new ArrayList<>());
    }

    public static Info fromJSON(JSONObject object) throws JSONException {
        String id = object.getString("id");
        String name = object.getString("name");
        String cover = object.getString("cover");
        String colorHex = object.getString("colorHex");
        String userId = object.getString("userId");
        JSONArray orderArray = object.getJSONArray("order");
        JSONArray queriesArray = object.getJSONArray("queries");
        List<String> order = new ArrayList<>();
        List<String> queries = new ArrayList<>();
        for (int i = 0; i < orderArray.length(); i++)
            order.add(orderArray.getString(i));
        for (int i = 0; i < queriesArray.length(); i++)
            queries.add(queriesArray.getString(i));
        return new Info(id, name, cover, colorHex, userId, order, queries);
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            object.put("name", name);
            object.put("cover", cover);
            object.put("colorHex", colorHex);
            object.put("userId", userId);
            object.put("order", new JSONArray(order));
            object.put("queries", new JSONArray(queries));
            return object;
        } catch (JSONException e) {
            throw new RuntimeException("Could not put items in JSON Object");
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> object = new HashMap<>();
        object.put("id", id);
        object.put("name", name);
        object.put("cover", cover);
        object.put("colorHex", colorHex);
        object.put("userId", userId);
        object.put("order", order);
        object.put("queries", queries);
        return object;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCover() {
        return cover;
    }

    public String getColorHex() {
        return colorHex;
    }

    public List<String> getOrder() {
        return order;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getQueries() {
        return queries;
    }

    @Override
    public @NotNull String toString() {
        return String.format("PlaylistInfo { id: '%s', name: '%s', cover: '%s' }", id, name, cover);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Info)) return false;
        Info that = (Info) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(cover, that.cover) &&
            Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, cover, order);
    }
}