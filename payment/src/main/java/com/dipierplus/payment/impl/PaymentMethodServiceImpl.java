package com.dipierplus.payment.impl;

import com.dipierplus.payment.model.*;
import com.dipierplus.payment.model.details.CardDetails;
import com.dipierplus.payment.model.details.CashPaymentDetails;
import com.dipierplus.payment.model.details.DigitalWalletDetails;
import com.dipierplus.payment.model.type.DigitalWalletType;
import com.dipierplus.payment.repository.PaymentMethodRepository;
import com.dipierplus.payment.service.PaymentMethodService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    // Patrones de validación
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{16}$"); // Para tarjetas de 16 dígitos
    private static final Pattern YAPE_PATTERN = Pattern.compile("^[0-9]{9}$"); // Para números de teléfono (9 dígitos)
    private static final Pattern PLIN_PATTERN = Pattern.compile("^[0-9]{9}$"); // Para números de teléfono (9 dígitos)

    @Override
    public List<PaymentMethod> getAllMethods(String customerId) {
        return paymentMethodRepository.findByCustomerId(customerId);
    }

    @Override
    public PaymentMethod addPaymentMethod(PaymentMethod paymentMethod) {
        validatePaymentMethod(paymentMethod);
        paymentMethod.setTransactionDate(new Date());
        return paymentMethodRepository.save(paymentMethod);
    }

    @Override
    public PaymentMethod updatePaymentMethod(String methodId, PaymentMethod paymentMethod) {
        validatePaymentMethod(paymentMethod);
        Optional<PaymentMethod> existingMethod = paymentMethodRepository.findById(methodId);
        if (existingMethod.isPresent()) {
            paymentMethod.setId(methodId);
            paymentMethod.setTransactionDate(new Date());
            return paymentMethodRepository.save(paymentMethod);
        } else {
            throw new NoSuchElementException("Payment method not found for ID: " + methodId);
        }
    }

    @Override
    public void deletePaymentMethod(String methodId) {
        paymentMethodRepository.deleteById(methodId);
    }


    private void validatePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod.getMethodType() == null) {
            throw new IllegalArgumentException("Payment method type cannot be null.");
        }

        switch (paymentMethod.getMethodType()) {
            case BANCA:
            case TARJETA_CREDITO:
            case TARJETA_DEBITO:
                for (PaymentDetails detail : paymentMethod.getDetails()) {
                    if (detail instanceof CardDetails) {
                        validateCardDetails((CardDetails) detail);
                    } else {
                        throw new IllegalArgumentException("Invalid payment details for card payment.");
                    }
                }
                break;

            case BILLETERA_DIGITAL:
                for (PaymentDetails detail : paymentMethod.getDetails()) {
                    if (detail instanceof DigitalWalletDetails) {
                        validateDigitalWalletDetails((DigitalWalletDetails) detail);
                    } else {
                        throw new IllegalArgumentException("Invalid payment details for digital wallet.");
                    }
                }
                break;

            case EFECTIVO:
                for (PaymentDetails detail : paymentMethod.getDetails()) {
                    if (detail instanceof CashPaymentDetails) {
                        validateCashPaymentDetails((CashPaymentDetails) detail);
                    } else {
                        throw new IllegalArgumentException("Invalid payment details for digital wallet.");
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid payment method type.");
        }
    }

    private void validateCashPaymentDetails(CashPaymentDetails cashPaymentDetails) {
        if (cashPaymentDetails.getCurrency() == null) {
            throw new IllegalArgumentException("Invalid currency format.");
        }
    }

    private void validateCardDetails(CardDetails cardDetails) {
        if (cardDetails.getCardNumber() == null ||
                !CARD_NUMBER_PATTERN.matcher(cardDetails.getCardNumber()).matches()) {
            throw new IllegalArgumentException("Invalid card number format.");
        }
    }

    private void validateDigitalWalletDetails(DigitalWalletDetails walletDetails) {
        String phoneNumber = walletDetails.getPhoneNumber();
        DigitalWalletType walletType = walletDetails.getWalletType();

        if (walletType == DigitalWalletType.YAPE) {
            if (phoneNumber == null || !YAPE_PATTERN.matcher(phoneNumber).matches()) {
                throw new IllegalArgumentException("Invalid Yape number format.");
            }
        } else if (walletType == DigitalWalletType.PLIN) {
            if (phoneNumber == null || !PLIN_PATTERN.matcher(phoneNumber).matches()) {
                throw new IllegalArgumentException("Invalid Plin number format.");
            }
        } else {
            throw new IllegalArgumentException("Invalid payment method type for digital wallet details.");
        }
    }
}