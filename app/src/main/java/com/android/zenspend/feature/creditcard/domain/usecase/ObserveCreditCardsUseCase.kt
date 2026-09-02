package com.android.zenspend.feature.creditcard.domain.usecase

import com.android.zenspend.feature.creditcard.domain.model.CreditCardSummary
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class ObserveCreditCardsUseCase @Inject constructor(
    private val repository: CreditCardRepository,
    private val emiRepository: EMIRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<CreditCardSummary>> {
        return repository.getAllActiveCreditCards().flatMapLatest { cards ->
            if (cards.isEmpty()) return@flatMapLatest flowOf(emptyList())

            val flows = cards.map { card ->
                combine(
                    repository.observeCreditCardOutstanding(card.walletId),
                    emiRepository.observeActiveEmiCountByWalletId(card.walletId)
                ) { outstanding, emiCount ->
                    val actualOutstanding = outstanding ?: 0L
                    CreditCardSummary(
                        card = card,
                        outstanding = actualOutstanding,
                        availableLimit = card.creditLimit - actualOutstanding,
                        utilization = if (card.creditLimit > 0) {
                            actualOutstanding.toDouble() / card.creditLimit
                        } else {
                            0.0
                        },
                        activeEmiCount = emiCount
                    )
                }
            }
            combine(flows) { it.toList() }
        }
    }
}
