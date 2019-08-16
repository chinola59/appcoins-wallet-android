package com.asfoundation.wallet.ui.gamification

import java.math.BigDecimal

data class UserRewardsStatus(val lastShownLevel: Int = 0, val level: Int = 0,
                             val toNextLevelAmount: BigDecimal = BigDecimal.ZERO,
                             val bonus: List<Double> = mutableListOf())
