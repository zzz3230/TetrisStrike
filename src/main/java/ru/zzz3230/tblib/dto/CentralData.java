package ru.zzz3230.tblib.dto;

import java.util.Date;

public record CentralData(String type, String version, Date time, Sign sign, Entry[] entries, Endpoint[] endpoints) {
    public record Sign(String envoy, String value){}
    public record Entry(String name, String netName, String[] endpoints, String status){}
    public record Endpoint(String name, String protocol, String primaryHost, String reserveHost){}
}
