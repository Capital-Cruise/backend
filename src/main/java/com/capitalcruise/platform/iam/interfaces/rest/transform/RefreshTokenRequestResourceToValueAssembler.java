package com.capitalcruise.platform.iam.interfaces.rest.transform;

import com.capitalcruise.platform.iam.interfaces.rest.resources.RefreshTokenRequestResource;

public final class RefreshTokenRequestResourceToValueAssembler {

    private RefreshTokenRequestResourceToValueAssembler() {
    }

    public static String toToken(RefreshTokenRequestResource resource) {
        return resource.refreshToken();
    }
}
