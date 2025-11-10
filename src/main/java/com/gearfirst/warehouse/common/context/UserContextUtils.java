package com.gearfirst.warehouse.common.context;

public final class UserContextUtils {
    private UserContextUtils() {}

    /**
     * Highest-priority rule: if current user's workType is "창고" and region is present,
     * use region as warehouseCode; otherwise keep the incoming value.
     */
    public static String effectiveWarehouseCode(String incoming) {
        UserContext uc = UserContextHolder.get();
        if (uc != null) {
            String wt = uc.getWorkType();
            String region = uc.getRegion();
            if (wt != null && wt.equals("창고") && region != null && !region.isBlank()) {
                return region;
            }
        }
        return incoming;
    }
}
