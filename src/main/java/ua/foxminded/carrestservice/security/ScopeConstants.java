package ua.foxminded.carrestservice.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScopeConstants {

    public static final String CARS_WRITE = "cars:write";
    public static final String CATALOG_WRITE = "catalog:write";
}
