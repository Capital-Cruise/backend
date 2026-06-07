package com.capitalcruise.platform.creditoperation.domain.model.valueobjects;

import java.math.BigDecimal;

public record ResolvedDownPayment(
        BigDecimal amount,
        BigDecimal percent
) {
}
