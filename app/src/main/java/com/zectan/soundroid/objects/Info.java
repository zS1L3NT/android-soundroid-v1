package com.zectan.soundroid.objects;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Info {
    private String id;
    private String name;
    private String cover;
    private List<String> order;
    private List<String> owners;

    public Info() {
    }

    public Info(String id, String name, List<String> order) {
        this.id = id;
        this.name = name;
        this.order = order;
    }

    public Info(String id, String name, String cover, List<String> order) {
        this.id = id;
        this.name = name;
        this.cover = cover;
        this.order = order;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public List<String> getOrder() {
        return order;
    }

    public void setOrder(List<String> order) {
        this.order = order;
    }

    public static Info getEmpty() {
        return new Info("", "", new ArrayList<>());
    }

    public List<String> getOwners() {
        return owners;
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