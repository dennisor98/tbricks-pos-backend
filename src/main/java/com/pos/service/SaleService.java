package com.pos.service;

import com.pos.dto.SaleDto;
import com.pos.dto.SaleItemDto;
import com.pos.entity.*;
import com.pos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public List<SaleDto> getAllSales() {
        return saleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public SaleDto getSaleById(String id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
        return convertToDto(sale);
    }

    public SaleDto getSaleByInvoiceNumber(String invoiceNumber) {
        Sale sale = saleRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
        return convertToDto(sale);
    }

    public List<SaleDto> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findByDateRange(startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SaleDto createSale(SaleDto saleDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Customer customer = null;


        Sale sale = Sale.builder()
                .invoiceNumber(generateInvoiceNumber())
                .customer(customer)
                .user(user)
                .subtotal(saleDto.getSubtotal())
                .taxAmount(saleDto.getTaxAmount())
                .discountAmount(saleDto.getDiscountAmount())
                .totalAmount(saleDto.getTotalAmount())
                .paymentMethod(saleDto.getPaymentMethod())
                .paymentStatus(saleDto.getPaymentStatus())
                .notes(saleDto.getNotes())
                .build();

        Sale savedSale = saleRepository.save(sale);

        for (SaleItemDto itemDto : saleDto.getSaleItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getQuantity() < itemDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - itemDto.getQuantity());
            productRepository.save(product);

            SaleItem saleItem = SaleItem.builder()
                    .sale(savedSale)
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .totalPrice(itemDto.getTotalPrice())
                    .discountAmount(itemDto.getDiscountAmount())
                    .build();

            saleItemRepository.save(saleItem);
        }

        return convertToDto(savedSale);
    }

    @Transactional
    public void refundSale(String saleId, List<String> itemIds) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        if (sale.getPaymentStatus() == Sale.PaymentStatus.REFUNDED) {
            throw new RuntimeException("Sale already refunded");
        }

        for (String itemId : itemIds) {
            SaleItem saleItem = saleItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Sale item not found"));

            Product product = saleItem.getProduct();
            product.setQuantity(product.getQuantity() + saleItem.getQuantity());
            productRepository.save(product);
        }

        if (itemIds.size() == sale.getSaleItems().size()) {
            sale.setPaymentStatus(Sale.PaymentStatus.REFUNDED);
        } else {
            sale.setPaymentStatus(Sale.PaymentStatus.PARTIALLY_REFUNDED);
        }

        saleRepository.save(sale);
    }

    private String generateInvoiceNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Random random = new Random();
        int randomNumber = 1000 + random.nextInt(9000);
        return "INV-" + datePrefix + "-" + randomNumber;
    }

    private SaleDto convertToDto(Sale sale) {
        List<SaleItemDto> saleItems = sale.getSaleItems().stream()
                .map(this::convertSaleItemToDto)
                .collect(Collectors.toList());

        return SaleDto.builder()
                .subtotal(sale.getSubtotal())
                .taxAmount(sale.getTaxAmount())
                .discountAmount(sale.getDiscountAmount())
                .totalAmount(sale.getTotalAmount())
                .paymentMethod(sale.getPaymentMethod())
                .paymentStatus(sale.getPaymentStatus())
                .notes(sale.getNotes())
                .saleItems(saleItems)
                .build();
    }

    private SaleItemDto convertSaleItemToDto(SaleItem saleItem) {
        return SaleItemDto.builder()
                .id(saleItem.getId())
                .productId(saleItem.getProduct().getId())
                .productName(saleItem.getProduct().getName())
                .productSku(saleItem.getProduct().getSku())
                .quantity(saleItem.getQuantity())
                .unitPrice(saleItem.getUnitPrice())
                .totalPrice(saleItem.getTotalPrice())
                .discountAmount(saleItem.getDiscountAmount())
                .build();
    }
}
