package com.android.zenspend.feature.creditcard.domain.usecase

import com.android.zenspend.feature.creditcard.domain.model.CreditCardDetails
import com.android.zenspend.feature.creditcard.domain.model.CreditCardSummary
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObserveCreditCardDetailsUseCase @Inject constructor(
    private val creditCardRepository: CreditCardRepository,
    private val emiRepository: EMIRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(cardId: Long): Flow<CreditCardDetails?> {
        return flow {
            emit(creditCardRepository.getCreditCardById(cardId))
        }.flatMapLatest { card ->
            if (card == null) {
                flowOf(null)
            } else {
                combine(
                    creditCardRepository.observeCreditCardOutstanding(card.walletId),
                    emiRepository.getActiveEMIsByWalletId(card.walletId)
                ) { outstanding, emis ->
                    val actualOutstanding = outstanding ?: 0L
                    CreditCardDetails(
                        summary = CreditCardSummary(
                            card = card,
                            outstanding = actualOutstanding,
                            availableLimit = card.creditLimit - actualOutstanding,
                            utilization = if (card.creditLimit > 0) {
                                actualOutstanding.toDouble() / card.creditLimit
                            } else {
                                0.0
                            },
                            activeEmiCount = emis.size
                        ),
                        activeEmis = emis
                    )
                }
            }
        }
    }
}
