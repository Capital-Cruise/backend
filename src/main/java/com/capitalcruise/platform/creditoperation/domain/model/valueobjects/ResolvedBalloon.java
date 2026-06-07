package com.capitalcruise.platform.creditoperation.domain.model.valueobjects;

import java.math.BigDecimal;

public record ResolvedBalloon(
        BigDecimal amount,
        BigDecimal percent
) {
}
