package com.autoservice.security;

public enum Role {
    ROLE_TRAVELER,
    ROLE_GUIDE,
    ROLE_ADMIN,
    ROLE_CUSTOMER,
    ROLE_MECHANIC;

    public Role normalized() {
        return switch (this) {
            case ROLE_CUSTOMER -> ROLE_TRAVELER;
            case ROLE_MECHANIC -> ROLE_GUIDE;
            default -> this;
        };
    }
}
